// Keep keyboard navigation state per combobox
const comboState = new WeakMap();

// open dropdown on clicking the select field
$(document).on('click', '.combobox-select', function () {
    const combo = $(this).closest('.combobox');
    combo.find('.combobox-list').show();
    combo.find('.combobox-search').focus();
});

// open dropdown on typing on the search field
$(document).on('keyup', '.combobox', function () {
    const combo = $(this).closest('.combobox');
    combo.find('.combobox-list').show();
    combo.find('.combobox-search').focus();
});

// Hide combobox on Tab key or focus lost
$(document).on('keydown', '.combobox-search', function (e) {
    if (e.key === "Tab") {
        $(this).closest('.combobox').find('.combobox-list').hide();
    }
});

$(document).on('blur', '.combobox-search', function () {
    const combo = $(this).closest('.combobox');
    setTimeout(() => {
        combo.find('.combobox-list').hide();
    }, 100);
});

// select item
$(document).on('click', '.combobox-list-item', function () {
    const combo = $(this).closest('.combobox');
    const value = $(this).text().trim();
    const oobParent = combo.closest('.combobox-oob-parent');
    const oobReset = combo.closest('.combobox-oob-reset');

    // Get all data-* attributes of the selected item
    const dataset = this.dataset; 

    Object.keys(dataset).forEach(key => {
        combo.find('.combobox-hidden-' + key).val(dataset[key]);
    });

    if (oobParent.length) {
        Object.keys(dataset).forEach(key => {
            oobParent.find('.combobox-oob-' + key).val(dataset[key]);
        });

        oobParent.find('.combobox-oob-reset').val(''); // trigger reset
    }

    

    combo.find('.combobox-select').val(value);
    combo.find('.combobox-list').hide();
});

// keyboard navigation
$(document).on('keydown', '.combobox-search', function (e) {
    const combo = $(this).closest('.combobox');
    const items = combo.find('.combobox-list-item');

    if (!comboState.has(combo[0])) comboState.set(combo[0], { index: -1 });
    const state = comboState.get(combo[0]);

    if (e.key === "ArrowDown") {
        state.index = (state.index + 1) % items.length;
        highlight(items, state.index);
        e.preventDefault();
    }
    else if (e.key === "ArrowUp") {
        state.index = (state.index - 1 + items.length) % items.length;
        highlight(items, state.index);
        e.preventDefault();
    }
    else if (e.key === "Enter") {
        if (state.index >= 0) items.eq(state.index).click();
        e.preventDefault();
    }
});

function highlight(items, index) {
    items.removeClass('keyboard-selected');

    if (index >= 0) {
        const item = items.eq(index);
        item.addClass('keyboard-selected');
        item[0].scrollIntoView({ block: "nearest" });
    }
}

// Click outside → close all combos
$(document).on('click', function (e) {
    if (!$(e.target).closest('.combobox').length) {
        $('.combobox-list').hide();
    }
});

// $(document).on('click', function (e) {
//     $('.combobox').each(function () {
//         if (!$(this).has(e.target).length) {
//             $(this).closest('.combobox-list').hide();
//         }
//     });
// });