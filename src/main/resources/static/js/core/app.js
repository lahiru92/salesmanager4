import { runInitializers } from './init-registry.js';
import * as purchaseOrder from '../modules/purchase-order.js';

// ACTIONS (user interaction)
const actions = {
    'po:add-item': () => purchaseOrder.addItem(),
    'po:remove-item': (e) => {
        purchaseOrder.removeItem(e.target);
    },
    'inv:add-item': () => invoice.addItem(),
};

document.body.addEventListener('click', (e) => {
    const action = e.target.closest('[data-action]')?.dataset.action;
    if (action && actions[action]) {
        actions[action](e);
    }
});

document.body.addEventListener('input', (e) => {
  const action = e.target.dataset.action;
  console.log('Input action:', action);
  if (action === 'po:subtotal') {
    purchaseOrder.subtotal(e);
  }
});

// INIT (HTMX lifecycle)
document.addEventListener('htmx:afterSettle', function (evt) {
    runInitializers(evt.target);
});

// HTMX Before request
document.body.addEventListener('htmx:beforeRequest', function (evt) {

    // Bootstrap form validation
    if (evt.target.closest('form.needs-validation')) {
        const form = evt.target.closest('form.needs-validation');
        if (form && !form.checkValidity()) {
            evt.preventDefault();
            form.classList.add('was-validated');
        }
    }

});