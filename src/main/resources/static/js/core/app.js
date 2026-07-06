import { runInitializers } from './init-registry.js';
import * as purchaseOrder from '../modules/purchase-order.js';
import * as grn from '../modules/grn.js';
import * as supplierPayment from '../modules/supplier-payment.js';

// ACTIONS (user interaction)
const actions = {
    'po:add-item': () => purchaseOrder.addItem(),
    'po:remove-item': (e) => {
        purchaseOrder.removeItem(e.target);
    },
    'grn:add-item': () => grn.addItem(),
    'grn:remove-item': (e) => {
        grn.removeItem(e.target);
    },
    'payment:fill': (e) => supplierPayment.fill(e)
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

  if (action === 'grn:subtotal') {
    grn.subtotal(e);
  }

  if (action === 'payment:recalc') {
    supplierPayment.recalc();
  }

  if (action === 'payment:method') {
    supplierPayment.toggleMethod();
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

    if (evt.target.getAttribute('hx-include')) {
        const includeSelector = evt.target.getAttribute('hx-include');
        const form = document.querySelector(includeSelector);
        if (form && !form.checkValidity()) {
            evt.preventDefault();
            form.classList.add('was-validated');
        }   
    }

});