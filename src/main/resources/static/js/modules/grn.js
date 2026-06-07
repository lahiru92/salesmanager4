let itemSelectInstance;
let supplierSelectInstance;

export function init(root) {
  initItemSelect(root);
  initSupplierSelect(root);
}

function initItemSelect(root) {
  const el = root.querySelector('#f-id');
  if (!el) return;

  if (el.tomselect) {
    el.tomselect.destroy();
  }

  itemSelectInstance = new TomSelect(el, {
    valueField: 'itemId',
    labelField: 'name',
    searchField: ['name'],

    load: function(query, callback) {
      if (!query.length) return callback();

      const supplierId = supplierSelectInstance ? supplierSelectInstance.getValue() : null;
      if (!supplierId) return callback();

      fetch('/items/api/list?q=' + query + '&supplierId=' + supplierId)
        .then(res => res.json())
        .then(json => callback(json))
        .catch(() => callback());
    }
  });
}

function initSupplierSelect(root) {
  const el = root.querySelector('#supplier');
  if (!el) return;

  if (el.tomselect) {
    el.tomselect.destroy();
  }

  supplierSelectInstance = new TomSelect(el, {
    valueField: 'supplierId',
    labelField: 'name',
    searchField: ['name'],

    load: function(query, callback) {
      if (!query.length) return callback();

      fetch('/suppliers/api/list?q=' + query)
        .then(res => res.json())
        .then(json => callback(json))
        .catch(() => callback());
    }
  });
}


export function subtotal(e) {
  const row = e.target.closest('tr');
  if (!row) return;

  const receivedQty  = parseFloat(row.querySelector('#f-received-qty')?.value) || 0;
  const rejectedQty = parseFloat(row.querySelector('#f-rejected-qty')?.value) || 0;
  const price = parseFloat(row.querySelector('#f-price')?.value) || 0;

  let acceptedQty = receivedQty - rejectedQty;

  row.querySelector('#f-accepted-qty').value = acceptedQty;
  row.querySelector('#f-subtotal').value = (acceptedQty * price).toFixed(2);
}

export function addItem() {
  const rowContainer = document.querySelector('#grn-row-container');
  const template = document.querySelector('#grn-row-template');

  if (!rowContainer || !template) {
    return;
  }

  const clone = template.content.firstElementChild.cloneNode(true);
  if (!clone) {
    return;
  }

  const itemId = itemSelectInstance.getValue();
  const itemData = itemSelectInstance.options[itemId];
  const receivedQty  = parseFloat(document.querySelector('#f-received-qty')?.value) || 0;
  const rejectedQty = parseFloat(document.querySelector('#f-rejected-qty')?.value) || 0;
  const price = parseFloat(document.querySelector('#f-price')?.value) || 0;

  clone.querySelector('.item-name').value = itemData ? itemData.name : itemId;
  clone.querySelector('.item-id').value = itemId;
  clone.querySelector('.received-qty').value = receivedQty;
  clone.querySelector('.rejected-qty').value = rejectedQty;
  clone.querySelector('.accepted-qty').value = receivedQty - rejectedQty;
  clone.querySelector('.price').value = price;
  clone.querySelector('.subtotal').innerText = ((receivedQty - rejectedQty) * price).toFixed(2);

  rowContainer.appendChild(clone);
}