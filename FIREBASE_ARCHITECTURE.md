# Cloud Firestore & Firebase Architecture Specification
## For MAK Pharma BD (Bangladesh Retail Pharmacy)

This document provides the full cloud architecture, Firestore schema, security rules, indexes, and Cloud Functions transaction patterns for syncing MAK Pharma BD to Firebase.

---

## 1. Firestore Data Collections

```
/pharmacy_settings/config
/users/{userId}
/medicines/{medicineId}
/medicineBatches/{batchId}
/suppliers/{supplierId}
/customers/{customerId}
/sales/{saleId}
/sales/{saleId}/saleItems/{itemId}
/purchases/{purchaseId}
/purchases/{purchaseId}/purchaseItems/{itemId}
/stockMovements/{movementId}
/customerDuePayments/{paymentId}
/expenses/{expenseId}
/cashierShifts/{shiftId}
/auditLogs/{logId}
```

### Key Field Schemas:

#### `/medicines/{medicineId}`
```json
{
  "name": "Napa Extra",
  "banglaName": "নাপা এক্সট্রা",
  "genericName": "Paracetamol + Caffeine",
  "brand": "Square Pharmaceuticals PLC",
  "dosageForm": "TABLET",
  "strength": "500mg + 65mg",
  "unit": "Strip",
  "packSize": 10,
  "category": "Analgesic & Antipyretic",
  "barcode": "894110010101",
  "sku": "MED-NAP-EXT",
  "prescriptionRequired": false,
  "reorderLevel": 30,
  "defaultSalePrice": 30.00,
  "isActive": true,
  "createdAt": "TIMESTAMP",
  "updatedAt": "TIMESTAMP"
}
```

#### `/medicineBatches/{batchId}`
```json
{
  "medicineId": "med_1",
  "batchNumber": "NE-2401",
  "mfgDate": "10/01/2024",
  "expiryDate": "18/11/2026",
  "expiryTimestampMillis": 1794960000000,
  "purchasePrice": 23.50,
  "mrp": 30.00,
  "salePrice": 30.00,
  "availableQty": 35,
  "supplierId": "sup_sqr",
  "supplierName": "Square Pharmaceuticals Ltd",
  "receivedDate": "15/01/2024",
  "storageLocation": "Rack A-1",
  "status": "ACTIVE"
}
```

#### `/sales/{saleId}`
```json
{
  "invoiceNumber": "PCB-20261026-0001",
  "timestamp": 1792998000000,
  "dateTimeStr": "26/10/2026 09:15 AM",
  "customerId": "cust_1",
  "customerName": "Md. Karim Ullah",
  "customerPhone": "01711-234567",
  "subtotal": 340.00,
  "discountAmount": 10.00,
  "taxAmount": 0.00,
  "grandTotal": 330.00,
  "paidAmount": 330.00,
  "dueAmount": 0.00,
  "changeReturn": 0.00,
  "paymentMethod": "CASH",
  "mobileBankingProvider": "",
  "cashierName": "Shuvo Rahman",
  "cashierRole": "CASHIER",
  "status": "COMPLETED",
  "itemsCount": 3
}
```

---

## 2. Firestore Security Rules (`firestore.rules`)

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // Helper functions
    function isAuthenticated() {
      return request.auth != null;
    }

    function getUserRole() {
      return get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role;
    }

    function isAdmin() {
      return isAuthenticated() && getUserRole() == "OWNER_ADMIN";
    }

    function isManager() {
      return isAuthenticated() && (getUserRole() in ["OWNER_ADMIN", "PHARMACIST_MANAGER"]);
    }

    function isStaff() {
      return isAuthenticated() && (getUserRole() in ["OWNER_ADMIN", "PHARMACIST_MANAGER", "CASHIER", "STOREKEEPER", "VIEWER_ACCOUNTANT"]);
    }

    // Public / Shared Settings
    match /pharmacy_settings/{docId} {
      allow read: if isAuthenticated();
      allow write: if isAdmin();
    }

    // Medicines and Batches
    match /medicines/{medicineId} {
      allow read: if isStaff();
      allow write: if isManager();
    }

    match /medicineBatches/{batchId} {
      allow read: if isStaff();
      allow write: if isManager() || (getUserRole() == "STOREKEEPER");
    }

    // Sales and Invoices
    match /sales/{saleId} {
      allow read: if isStaff();
      allow create: if isStaff();
      // Only manager/admin can refund
      allow update: if isManager() && request.resource.data.status == "REFUNDED";
      allow delete: if false; // Immutable ledger
    }

    match /sales/{saleId}/saleItems/{itemId} {
      allow read: if isStaff();
      allow create: if isStaff();
      allow update, delete: if false;
    }

    // Stock Movements & Audit Logs (Append-Only)
    match /stockMovements/{movementId} {
      allow read: if isStaff();
      allow create: if isStaff();
      allow update, delete: if false;
    }

    match /auditLogs/{logId} {
      allow read: if isManager();
      allow create: if isStaff();
      allow update, delete: if false;
    }

    // Customers and Due Collections
    match /customers/{customerId} {
      allow read, write: if isStaff();
    }

    match /customerDuePayments/{paymentId} {
      allow read: if isStaff();
      allow create: if isStaff();
      allow update, delete: if false;
    }

    // Suppliers & Purchases
    match /suppliers/{supplierId} {
      allow read: if isStaff();
      allow write: if isManager();
    }

    match /purchases/{purchaseId} {
      allow read: if isStaff();
      allow create: if isManager() || (getUserRole() == "STOREKEEPER");
      allow update, delete: if isAdmin();
    }

    // Expenses & Shifts
    match /expenses/{expenseId} {
      allow read: if isStaff();
      allow create: if isStaff();
      allow update, delete: if isManager();
    }

    match /cashierShifts/{shiftId} {
      allow read, write: if isStaff();
    }

    // Users collection
    match /users/{userId} {
      allow read: if isStaff();
      allow write: if isAdmin();
    }
  }
}
```

---

## 3. Firestore Compound Indexes (`firestore.indexes.json`)

```json
{
  "indexes": [
    {
      "collectionGroup": "medicineBatches",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "medicineId", "order": "ASCENDING" },
        { "fieldPath": "status", "order": "ASCENDING" },
        { "fieldPath": "expiryTimestampMillis", "order": "ASCENDING" }
      ]
    },
    {
      "collectionGroup": "sales",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "status", "order": "ASCENDING" },
        { "fieldPath": "timestamp", "order": "DESCENDING" }
      ]
    },
    {
      "collectionGroup": "stockMovements",
      "queryScope": "COLLECTION",
      "fields": [
        { "fieldPath": "medicineId", "order": "ASCENDING" },
        { "fieldPath": "timestamp", "order": "DESCENDING" }
      ]
    }
  ]
}
```

---

## 4. Atomic Cloud Function Transaction (Node.js TypeScript Reference)

For server-side execution of POS sale with race-condition prevention and stock decrement:

```typescript
import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

admin.initializeApp();
const db = admin.firestore();

export const processSaleTransaction = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError("unauthenticated", "Must be authenticated");
  }

  const { sale, items } = data;

  return await db.runTransaction(async (transaction) => {
    // 1. Validate all batch stocks
    for (const item of items) {
      const batchRef = db.collection("medicineBatches").doc(item.batchId);
      const batchDoc = await transaction.get(batchRef);

      if (!batchDoc.exists) {
        throw new functions.https.HttpsError("not-found", `Batch ${item.batchId} not found`);
      }

      const currentQty = batchDoc.data()?.availableQty || 0;
      if (currentQty < item.quantity) {
        throw new functions.https.HttpsError(
          "failed-precondition",
          `Insufficient stock in batch ${batchDoc.data()?.batchNumber}. Available: ${currentQty}, Requested: ${item.quantity}`
        );
      }

      // Decrement stock
      const newQty = currentQty - item.quantity;
      transaction.update(batchRef, {
        availableQty: newQty,
        status: newQty === 0 ? "OUT_OF_STOCK" : "ACTIVE",
        updatedAt: admin.firestore.FieldValue.serverTimestamp()
      });

      // Stock movement record
      const movementRef = db.collection("stockMovements").doc();
      transaction.set(movementRef, {
        timestamp: Date.now(),
        medicineId: item.medicineId,
        medicineName: item.medicineName,
        batchId: item.batchId,
        batchNumber: item.batchNumber,
        type: "SALE",
        quantityChange: -item.quantity,
        remainingQty: newQty,
        referenceId: sale.invoiceNumber,
        performedBy: context.auth.token.name || "Cashier"
      });
    }

    // 2. Adjust customer due balance if applicable
    if (sale.customerId !== "WALK_IN" && sale.dueAmount > 0) {
      const custRef = db.collection("customers").doc(sale.customerId);
      transaction.update(custRef, {
        currentDue: admin.firestore.FieldValue.increment(sale.dueAmount)
      });
    }

    // 3. Write sale record
    const saleRef = db.collection("sales").doc(sale.id);
    transaction.set(saleRef, {
      ...sale,
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    });

    return { success: true, invoiceNumber: sale.invoiceNumber };
  });
});
```
