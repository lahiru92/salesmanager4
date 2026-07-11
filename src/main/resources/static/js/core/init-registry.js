import * as purchaseOrder from '../modules/purchase-order.js';
import * as grn from '../modules/grn.js';
import * as invoice from '../modules/invoice.js';
import * as supplierPayment from '../modules/supplier-payment.js';
import * as customerPayment from '../modules/customer-payment.js';

const registry = {
    'purchase-order': purchaseOrder,
    'grn': grn,
    'invoice': invoice,
    'supplier-payment': supplierPayment,
    'customer-payment': customerPayment
};

export function runInitializers(root) {
    console.log('Running initializers for', root);
    const el = root.querySelector('[data-init]');
    if (!el) return;

    const key = el.dataset.init;
    const module = registry[key];

    if (module && module.init) {
        module.init(el);
    }
}
