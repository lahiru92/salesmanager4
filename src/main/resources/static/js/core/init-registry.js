import * as purchaseOrder from '../modules/purchase-order.js';

const registry = {
    'purchase-order': purchaseOrder
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