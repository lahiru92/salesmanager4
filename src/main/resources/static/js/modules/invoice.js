let itemSelectInstance;
let customerSelectInstance;

export function init(root) {
  initItemSelect(root);
  initCustomerSelect(root);
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

      fetch('/items/api/list?q=' + query)
        .then(res => res.json())
        .then(json => callback(json))
        .catch(() => callback());
    }
  });
}

function initCustomerSelect(root) {
  const el = root.querySelector('#customer');
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
    }
  });
}


export function subtotal(e) {
  const row = e.target.closest('tr');
  if (!row) return;

  const qty = parseFloat(row.querySelector('.qty')?.value) || 0;
  const price = parseFloat(row.querySelector('.unit-price')?.value) || 0;
  const discount = parseFloat(row.querySelector('.discount')?.value) || 0;

  row.querySelector('.subtotal').value = (qty * price - discount).toFixed(2);
  grandTotal();
}

export function addItem() {
  const rowContainer = document.querySelector('#invoice-row-container');
  const template = document.querySelector('#invoice-row-template');

  if (!rowContainer || !template) {
    return;
  }

  const clone = template.content.firstElementChild.cloneNode(true);
  if (!clone) {
    return;
  }

  const itemId = itemSelectInstance.getValue();
  const itemData = itemSelectInstance.options[itemId];
  const qty = parseFloat(document.querySelector('#f-qty')?.value) || 0;
  const freeQty = parseFloat(document.querySelector('#f-free-qty')?.value) || 0;
  const price = parseFloat(document.querySelector('#f-price')?.value) || 0;
  const discount = parseFloat(document.querySelector('#f-discount')?.value) || 0;

  clone.querySelector('.item-name').value = itemData ? itemData.name : itemId;
  clone.querySelector('.item-id').value = itemId;
  clone.querySelector('.qty').value = qty;
  clone.querySelector('.free-qty').value = freeQty;
  clone.querySelector('.unit-price').value = price;
  clone.querySelector('.discount').value = discount;
  clone.querySelector('.subtotal').value = (qty * price - discount).toFixed(2);

  rowContainer.appendChild(clone);

  // Clear input fields
  document.querySelector('#f-id')?.tomselect.clear();
  document.querySelector('#f-qty').value = '';
  document.querySelector('#f-free-qty').value = '';
  document.querySelector('#f-price').value = '';
  document.querySelector('#f-discount').value = '';
  document.querySelector('#f-subtotal').value = '';

  reindex();
  grandTotal();
}

export function reindex() {
  const rows = document.querySelectorAll('.invoice-row');
  rows.forEach((row, index) => {
    row.querySelector('.item-name').setAttribute('name', `items[${index}].itemName`);
    row.querySelector('.item-id').setAttribute('name', `items[${index}].itemId`);
    row.querySelector('.qty').setAttribute('name', `items[${index}].quantity`);
    row.querySelector('.free-qty').setAttribute('name', `items[${index}].freeQty`);
    row.querySelector('.unit-price').setAttribute('name', `items[${index}].unitPrice`);
    row.querySelector('.discount').setAttribute('name', `items[${index}].discount`);
  });
}

export function grandTotal() {
  let total = 0;
  const rows = document.querySelectorAll('.invoice-row');
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
