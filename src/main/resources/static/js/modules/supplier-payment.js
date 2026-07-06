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

export function toggleMethod() {
  const method = document.querySelector('#paymentMethod')?.value;

  document.querySelectorAll('.payment-field').forEach(field => {
    const show = (field.dataset.methods || '').split(' ').includes(method);
    field.classList.toggle('d-none', !show);
    field.querySelectorAll('input').forEach(input => input.disabled = !show);
  });
}

export function recalc() {
  const amount = parseFloat(document.querySelector('#totalPaymentAmount')?.value) || 0;

  let allocated = 0;
  document.querySelectorAll('.allocation-input').forEach(input => {
    allocated += parseFloat(input.value) || 0;
  });

  const allocatedEl = document.querySelector('#allocated-total');
  const unallocatedEl = document.querySelector('#unallocated-total');

  if (allocatedEl) allocatedEl.innerText = allocated.toFixed(2);

  if (unallocatedEl) {
    const remaining = amount - allocated;
    unallocatedEl.innerText = remaining.toFixed(2);
    unallocatedEl.classList.toggle('text-danger', remaining < 0);
    unallocatedEl.classList.toggle('fw-bold', remaining < 0);
  }
}

export function fill(e) {
  const row = e.target.closest('tr');
  if (!row) return;

  const input = row.querySelector('.allocation-input');
  const outstanding = parseFloat(row.dataset.outstanding) || 0;
  const amount = parseFloat(document.querySelector('#totalPaymentAmount')?.value) || 0;

  let allocatedOther = 0;
  document.querySelectorAll('.allocation-input').forEach(other => {
    if (other !== input) allocatedOther += parseFloat(other.value) || 0;
  });

  const cap = amount > 0 ? Math.max(amount - allocatedOther, 0) : outstanding;
  input.value = Math.min(outstanding, cap).toFixed(2);
  recalc();
}
