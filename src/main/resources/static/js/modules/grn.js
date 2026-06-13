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

    load: function (query, callback) {
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

    load: function (query, callback) {
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

  const receivedQty = parseFloat(row.querySelector('.received-qty')?.value) || 0;
  const rejectedQty = parseFloat(row.querySelector('.rejected-qty')?.value) || 0;
  const price = parseFloat(row.querySelector('.unit-price')?.value) || 0;

  let acceptedQty = receivedQty - rejectedQty;

  row.querySelector('.accepted-qty').value = acceptedQty;
  row.querySelector('.subtotal').value = (acceptedQty * price).toFixed(2);
  grandTotal();
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
  const receivedQty = parseFloat(document.querySelector('#f-received-qty')?.value) || 0;
  const rejectedQty = parseFloat(document.querySelector('#f-rejected-qty')?.value) || 0;
  const price = parseFloat(document.querySelector('#f-price')?.value) || 0;

  clone.querySelector('.item-name').value = itemData ? itemData.name : itemId;
  clone.querySelector('.item-id').value = itemId;
  clone.querySelector('.received-qty').value = receivedQty;
  clone.querySelector('.rejected-qty').value = rejectedQty;
  clone.querySelector('.accepted-qty').value = receivedQty - rejectedQty;
  clone.querySelector('.unit-price').value = price;
  clone.querySelector('.subtotal').value = ((receivedQty - rejectedQty) * price).toFixed(2);

  rowContainer.appendChild(clone);

  // Clear input fields
  document.querySelector('#f-id')?.tomselect.clear();
  document.querySelector('#f-received-qty').value = '';
  document.querySelector('#f-rejected-qty').value = '';
  document.querySelector('#f-accepted-qty').value = '';
  document.querySelector('#f-price').value = '';
  document.querySelector('#f-subtotal').value = '';

  reindex();
  grandTotal();
}

export function reindex() {
  const rows = document.querySelectorAll('.grn-row');
  rows.forEach((row, index) => {
    row.querySelector('.item-name').setAttribute('name', `items[${index}].itemName`);
    row.querySelector('.item-id').setAttribute('name', `items[${index}].itemId`);
    row.querySelector('.received-qty').setAttribute('name', `items[${index}].receivedQty`);
    row.querySelector('.rejected-qty').setAttribute('name', `items[${index}].rejectedQty`);
    row.querySelector('.accepted-qty').setAttribute('name', `items[${index}].acceptedQty`);
    row.querySelector('.unit-price').setAttribute('name', `items[${index}].unitPrice`);
    // row.querySelector('.ordered-price').setAttribute('name', `items[${index}].orderedPrice`);
    row.querySelector('.subtotal').setAttribute('name', `items[${index}].subtotal`);
  });
}

export function grandTotal() {
  let total = 0;
  const rows = document.querySelectorAll('.grn-row');
  rows.forEach(row => {
    const sub = parseFloat(row.querySelector('.subtotal')?.value) || 0;
    total += sub;
  });
  document.getElementById('grand-total').innerText = total.toFixed(2);
}

export function removeItem(btn) {
  btn.closest('tr').remove();
  reindex();
  grandTotal();
}