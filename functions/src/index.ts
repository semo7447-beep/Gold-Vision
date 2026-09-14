import {onSchedule} from "firebase-functions/v2/scheduler";
import {onRequest} from "firebase-functions/v2/https";
import {defineSecret} from "firebase-functions/params";
import {initializeApp} from "firebase-admin/app";
import {getFirestore, FieldValue} from "firebase-admin/firestore";
import * as logger from "firebase-functions/logger";

// خادم وسيط واحد بين التطبيق ومزوّد بيانات الذهب (GoldAPI.io): يجلب
// السعر مرة واحدة كل فترة ويخزّنه، وكل مستخدمي التطبيق (مهما كان
// عددهم) يقرؤون من هذا التخزين المشترك بدل الاتصال بالمزوّد كل واحد
// لحاله — هذا يبقي عدد الطلبات الفعلية على المزوّد ثابتاً وقليلاً بغض
// النظر عن عدد مستخدمي التطبيق، ويخفي مفتاح API عن التطبيق نفسه (لا
// يمكن لأي شخص استخراجه من ملف APK واستخدامه بدل الدفع)
initializeApp();
const db = getFirestore();

// يُضبط مرة واحدة عبر: firebase functions:secrets:set GOLDAPI_KEY
const goldApiKey = defineSecret("GOLDAPI_KEY");

const TROY_OUNCE_GRAMS = 31.1034768;
const REGION = "us-central1";

function isPositiveNumber(value: unknown): value is number {
  return typeof value === "number" && value > 0;
}

// ==================== السعر الحي ====================

// تعمل تلقائياً كل ساعة — تجلب سعر أونصة الذهب بالدولار وتخزّنه
export const refreshGoldPrice = onSchedule(
  {schedule: "every 60 minutes", region: REGION, secrets: [goldApiKey]},
  async () => {
    let rawBody = "";
    try {
      const res = await fetch("https://www.goldapi.io/api/price/XAU/USD", {
        headers: {
          "x-access-token": goldApiKey.value(),
          "Content-Type": "application/json",
        },
      });
      rawBody = await res.text();
      if (!res.ok) {
        logger.error("refreshGoldPrice: HTTP error", {status: res.status, body: rawBody.slice(0, 500)});
        return;
      }
      const json = JSON.parse(rawBody);
      const priceUsdOz = json.price;
      if (!isPositiveNumber(priceUsdOz)) {
        logger.error("refreshGoldPrice: unexpected response shape", {body: rawBody.slice(0, 500)});
        return;
      }
      await db.collection("market").doc("live").set({
        pricePerOunceUsd: priceUsdOz,
        updatedAt: FieldValue.serverTimestamp(),
      });
    } catch (e) {
      logger.error("refreshGoldPrice: exception", {message: String(e), body: rawBody.slice(0, 500)});
    }
  }
);

// يستدعيها التطبيق (بدل الاتصال بالمزوّد مباشرة) — تُعيد آخر سعر
// مخزَّن بنفس الشكل ("per_gram_usd") الذي يتوقعه GoldMarket.kt أصلاً،
// حتى لا يحتاج أي تعديل بمنطق التحليل داخل التطبيق، فقط تغيير الرابط
export const getGoldPrice = onRequest(
  {region: REGION, cors: true},
  async (req, res) => {
    try {
      const snap = await db.collection("market").doc("live").get();
      if (!snap.exists) {
        res.status(503).json({error: "not_ready"});
        return;
      }
      const pricePerOunceUsd = snap.data()?.pricePerOunceUsd as number;
      if (!isPositiveNumber(pricePerOunceUsd)) {
        res.status(503).json({error: "not_ready"});
        return;
      }
      res.set("Cache-Control", "public, max-age=300");
      res.status(200).json({per_gram_usd: pricePerOunceUsd / TROY_OUNCE_GRAMS});
    } catch (e) {
      logger.error("getGoldPrice: exception", {message: String(e)});
      res.status(500).json({error: "internal_error"});
    }
  }
);

// ==================== التاريخ اليومي ====================

function isoDate(d: Date): string {
  return d.toISOString().slice(0, 10);
}

// تعمل تلقائياً مرة كل يوم — تجلب شمعة يوم أمس وتضيفها لسجل تراكمي
// (يُبنى تدريجياً يوماً بيوم بدل استيراد سنوات كاملة دفعة واحدة، حتى
// لا نستهلك حصة الطلبات الشهرية على شكل تحميل ضخم لمرة واحدة)
export const refreshGoldHistory = onSchedule(
  {schedule: "every day 00:30", timeZone: "UTC", region: REGION, secrets: [goldApiKey]},
  async () => {
    let rawBody = "";
    try {
      const yesterday = new Date(Date.now() - 24 * 60 * 60 * 1000);
      const dateParam = isoDate(yesterday).replace(/-/g, ""); // YYYYMMDD

      const res = await fetch(`https://www.goldapi.io/api/XAU/USD/${dateParam}`, {
        headers: {
          "x-access-token": goldApiKey.value(),
          "Content-Type": "application/json",
        },
      });
      rawBody = await res.text();
      if (!res.ok) {
        logger.error("refreshGoldHistory: HTTP error", {status: res.status, body: rawBody.slice(0, 500)});
        return;
      }
      const json = JSON.parse(rawBody);
      const open = json.open_price ?? json.prev_close_price;
      const close = json.price ?? json.close_price;
      const high = json.high_price;
      const low = json.low_price;
      if (![open, close, high, low].every(isPositiveNumber)) {
        logger.error("refreshGoldHistory: unexpected response shape", {body: rawBody.slice(0, 500)});
        return;
      }

      const docRef = db.collection("market").doc("history");
      await db.runTransaction(async (tx) => {
        const snap = await tx.get(docRef);
        const existing = (snap.exists ? snap.data()?.bars : []) as
          | {date: string; open: number; high: number; low: number; close: number}[]
          | undefined;
        const bars = existing ?? [];
        const dateStr = isoDate(yesterday);
        const withoutToday = bars.filter((b) => b.date !== dateStr);
        withoutToday.push({date: dateStr, open, high, low, close});
        withoutToday.sort((a, b) => a.date.localeCompare(b.date));
        // نحتفظ بآخر 1825 يوماً (5 سنوات) فقط حتى لا يتضخم المستند بلا حدود
        const trimmed = withoutToday.slice(-1825);
        tx.set(docRef, {bars: trimmed, updatedAt: FieldValue.serverTimestamp()});
      });
    } catch (e) {
      logger.error("refreshGoldHistory: exception", {message: String(e), body: rawBody.slice(0, 500)});
    }
  }
);

// يستدعيها التطبيق — تُعيد كل الشموع المتراكمة حتى الآن بنفس الشكل
// ("bars" ثم date/open/high/low/close) الذي يتوقعه GoldHistory.kt أصلاً
export const getGoldHistory = onRequest(
  {region: REGION, cors: true},
  async (req, res) => {
    try {
      const snap = await db.collection("market").doc("history").get();
      const bars = snap.exists ? snap.data()?.bars ?? [] : [];
      res.set("Cache-Control", "public, max-age=3600");
      res.status(200).json({bars});
    } catch (e) {
      logger.error("getGoldHistory: exception", {message: String(e)});
      res.status(500).json({error: "internal_error"});
    }
  }
);
