export function init(root) {
  initSupplierSelect(root);
}

function initSupplierSelect(root) {
  const el = root.querySelector('#ledger-supplier');
  if (!el) return;

  if (el.tomselect) {
    el.tomselect.destroy();
  }

  new TomSelect(el, {
    valueField: 'supplierId',
    labelField: 'name',
    searchField: ['name'],
    allowEmptyOption: true,

    load: function (query, callback) {
      if (!query.length) return callback();

      fetch('/suppliers/api/list?q=' + query)
        .then(res => res.json())
        .then(json => callback(json))
        .catch(() => callback());
    }
  });
}
