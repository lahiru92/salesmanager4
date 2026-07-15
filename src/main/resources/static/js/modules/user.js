export function init(root) {
  initEmployeeSelect(root);
}

function initEmployeeSelect(root) {
  const el = root.querySelector('#user-employee');
  if (!el) return;

  if (el.tomselect) {
    el.tomselect.destroy();
  }

  new TomSelect(el, {
    valueField: 'id',
    labelField: 'knownName',
    searchField: ['knownName'],
    allowEmptyOption: true,

    load: function (query, callback) {
      if (!query.length) return callback();

      fetch('/employees/api/list?q=' + query)
        .then(res => res.json())
        .then(json => callback(json))
        .catch(() => callback());
    }
  });
}
