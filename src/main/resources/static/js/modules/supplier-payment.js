import { toggleMethod, recalc } from './payment-allocation.js';

let supplierSelectInstance;

export function init(root) {
  initSupplierSelect(root);
  toggleMethod();
  recalc();
}

function initSupplierSelect(root) {
  const el = root.querySelector('#payment-supplier');
  if (!el) return;

  if (el.tomselect) {
    el.tomselect.destroy();
  }

  supplierSelectInstance = new TomSelect(el, {
    valueField: 'supplierId',
    labelField: 'name',
    searchField: ['name'],

    load: function (query, callback) {
      if (!query.length) return callback();

      fetch('/suppliers/api/list?q=' + query)
        .then(res => res.json())
        .then(json => callback(json))
        .catch(() => callback());
    },

    onChange: function (value) {
      if (!value) return;

      htmx.ajax('GET', '/supplier-payments/outstanding-grns?supplierId=' + value, {
        target: '#allocation-container',
        swap: 'innerHTML'
      }).then(() => recalc());
    }
  });
}
