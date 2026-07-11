import { toggleMethod, recalc } from './payment-allocation.js';

let customerSelectInstance;

export function init(root) {
  initCustomerSelect(root);
  toggleMethod();
  recalc();
}

function initCustomerSelect(root) {
  const el = root.querySelector('#payment-customer');
  if (!el) return;

  if (el.tomselect) {
    el.tomselect.destroy();
  }

  customerSelectInstance = new TomSelect(el, {
    valueField: 'customerId',
    labelField: 'name',
    searchField: ['name'],

    load: function (query, callback) {
      if (!query.length) return callback();

      fetch('/customers/api/list?q=' + query)
        .then(res => res.json())
        .then(json => callback(json))
        .catch(() => callback());
    },

    onChange: function (value) {
      if (!value) return;

      htmx.ajax('GET', '/customer-payments/outstanding-invoices?customerId=' + value, {
        target: '#allocation-container',
        swap: 'innerHTML'
      }).then(() => recalc());
    }
  });
}
