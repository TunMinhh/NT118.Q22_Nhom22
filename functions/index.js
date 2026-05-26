const crypto = require("crypto");
const admin = require("firebase-admin");
const {setGlobalOptions} = require("firebase-functions");
const {onRequest} = require("firebase-functions/https");
const {defineSecret} = require("firebase-functions/params");

admin.initializeApp();
setGlobalOptions({maxInstances: 10, region: "asia-southeast1"});

const vnpayTmnCode = defineSecret("VNPAY_TMN_CODE");
const vnpayHashSecret = defineSecret("VNPAY_HASH_SECRET");

const VNPAY_PAY_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";

function formatVnpayDate(date) {
  const parts = new Intl.DateTimeFormat("en-CA", {
    timeZone: "Asia/Ho_Chi_Minh",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    second: "2-digit",
    hour12: false,
  }).formatToParts(date);

  const value = {};
  parts.forEach((part) => {
    value[part.type] = part.value;
  });

  return `${value.year}${value.month}${value.day}${value.hour}${value.minute}${value.second}`;
}

function vnpayEncode(value) {
  return encodeURIComponent(String(value)).replace(/%20/g, "+");
}

function sortObject(input) {
  const sorted = {};
  const keys = [];

  Object.keys(input).forEach((key) => {
    if (input[key] !== undefined && input[key] !== null && input[key] !== "") {
      keys.push(encodeURIComponent(key));
    }
  });

  keys.sort();
  keys.forEach((key) => {
    sorted[key] = vnpayEncode(input[key]);
  });

  return sorted;
}

function normalizeVnpayText(value, fallback) {
  return String(value || fallback)
      .normalize("NFD")
      .replace(/[\u0300-\u036f]/g, "")
      .replace(/\u0111/g, "d")
      .replace(/\u0110/g, "D")
      .replace(/[^a-zA-Z0-9 ]/g, " ")
      .replace(/\s+/g, " ")
      .trim()
      .slice(0, 255);
}

function normalizeTxnRef(value) {
  const normalized = String(value || "")
      .replace(/[^a-zA-Z0-9]/g, "")
      .slice(0, 100);
  return normalized || `ORDER${Date.now()}`;
}

function normalizeIpAddress(value) {
  const ip = String(value || "").trim();
  return /^\d{1,3}(\.\d{1,3}){3}$/.test(ip) ? ip : "127.0.0.1";
}

function buildQuery(params) {
  return Object.entries(sortObject(params))
      .map(([key, value]) => `${key}=${value}`)
      .join("&");
}

function signParams(params, hashSecret) {
  const signData = buildQuery(params);
  return crypto
      .createHmac("sha512", hashSecret)
      .update(Buffer.from(signData, "utf-8"))
      .digest("hex");
}

function verifyVnpayParams(params, hashSecret) {
  const secureHash = params.vnp_SecureHash;
  const clonedParams = {...params};
  delete clonedParams.vnp_SecureHash;
  delete clonedParams.vnp_SecureHashType;

  const expectedHash = signParams(clonedParams, hashSecret);
  return secureHash && expectedHash.toLowerCase() === String(secureHash).toLowerCase();
}

function allowCors(req, res) {
  res.set("Access-Control-Allow-Origin", "*");
  res.set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
  res.set("Access-Control-Allow-Headers", "Content-Type, Authorization");

  if (req.method === "OPTIONS") {
    res.status(204).send("");
    return true;
  }

  return false;
}

exports.createVnpayPayment = onRequest(
    {secrets: [vnpayTmnCode, vnpayHashSecret], invoker: "public"},
    async (req, res) => {
      if (allowCors(req, res)) return;

      if (req.method !== "POST") {
        res.status(405).json({message: "Method not allowed"});
        return;
      }

      const {
        amount,
        orderId,
        orderInfo,
        returnUrl,
        bankCode,
        locale = "vn",
      } = req.body || {};

      if (!amount || !orderId || !returnUrl) {
        res.status(400).json({
          message: "Missing required fields: amount, orderId, returnUrl",
        });
        return;
      }

      const numericAmount = Number(amount);
      if (!Number.isFinite(numericAmount) || numericAmount <= 0) {
        res.status(400).json({message: "Invalid amount"});
        return;
      }

      const now = new Date();
      const expireAt = new Date(now.getTime() + 15 * 60 * 1000);
      const rawIpAddress =
        req.headers["x-forwarded-for"]?.split(",")[0]?.trim() ||
        req.socket.remoteAddress ||
        "127.0.0.1";
      const safeOrderId = normalizeTxnRef(orderId);
      const safeOrderInfo = normalizeVnpayText(orderInfo, `Thanh toan don hang ${safeOrderId}`);
      const ipAddress = normalizeIpAddress(rawIpAddress);

      const params = {
        vnp_Version: "2.1.0",
        vnp_Command: "pay",
        vnp_TmnCode: vnpayTmnCode.value(),
        vnp_Amount: Math.round(numericAmount * 100),
        vnp_CurrCode: "VND",
        vnp_TxnRef: safeOrderId,
        vnp_OrderInfo: safeOrderInfo,
        vnp_OrderType: "other",
        vnp_Locale: locale,
        vnp_ReturnUrl: returnUrl,
        vnp_IpAddr: ipAddress,
        vnp_CreateDate: formatVnpayDate(now),
        vnp_ExpireDate: formatVnpayDate(expireAt),
      };

      if (bankCode) {
        params.vnp_BankCode = bankCode;
      }

      params.vnp_SecureHash = signParams(params, vnpayHashSecret.value());
      const paymentUrl = `${VNPAY_PAY_URL}?${buildQuery(params)}`;

      await admin.firestore().collection("payments").doc(safeOrderId).set({
        amount: numericAmount,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
        gateway: "VNPAY",
        originalOrderId: String(orderId),
        orderId: safeOrderId,
        orderInfo: params.vnp_OrderInfo,
        status: "created",
      }, {merge: true});

      res.json({paymentUrl, orderId: safeOrderId});
    },
);

exports.vnpayReturn = onRequest(
    {secrets: [vnpayHashSecret], invoker: "public"},
    async (req, res) => {
      const params = req.query || {};
      const isValid = verifyVnpayParams(params, vnpayHashSecret.value());
      const orderId = params.vnp_TxnRef;
      const isSuccess = isValid && params.vnp_ResponseCode === "00";

      if (orderId) {
        await admin.firestore().collection("payments").doc(String(orderId)).set({
          bankCode: params.vnp_BankCode || "",
          paidAt: admin.firestore.FieldValue.serverTimestamp(),
          responseCode: params.vnp_ResponseCode || "",
          status: isSuccess ? "success" : "failed",
          transactionNo: params.vnp_TransactionNo || "",
          verified: isValid,
        }, {merge: true});
      }

      res.status(isSuccess ? 200 : 400).send(
          isSuccess ?
            "Thanh toán VNPAY thành công. Bạn có thể quay lại ứng dụng." :
            "Thanh toán VNPAY thất bại hoặc chữ ký không hợp lệ.",
      );
    },
);

exports.vnpayIpn = onRequest(
    {secrets: [vnpayHashSecret], invoker: "public"},
    async (req, res) => {
      const params = req.query || {};
      const isValid = verifyVnpayParams(params, vnpayHashSecret.value());
      const orderId = params.vnp_TxnRef;

      if (!isValid) {
        res.json({RspCode: "97", Message: "Invalid signature"});
        return;
      }

      if (!orderId) {
        res.json({RspCode: "01", Message: "Order not found"});
        return;
      }

      const isSuccess = params.vnp_ResponseCode === "00";

      await admin.firestore().collection("payments").doc(String(orderId)).set({
        bankCode: params.vnp_BankCode || "",
        paidAt: admin.firestore.FieldValue.serverTimestamp(),
        responseCode: params.vnp_ResponseCode || "",
        status: isSuccess ? "success" : "failed",
        transactionNo: params.vnp_TransactionNo || "",
        verified: true,
      }, {merge: true});

      res.json({RspCode: "00", Message: "Confirm Success"});
    },
);
