document.body.addEventListener("showToast", function (evt) {
    let bgColor = "#333333"; 
    let textColor = "#FFFFFF"; 

    switch (evt.detail.type) {
        case "info":
        case "success":
            bgColor = "#2196F3";
            break;
        case "warning":
            bgColor = "#FF9800";
            textColor = "#000000"; 
            break;
        case "error":
            bgColor = "#F44336";
            break;
    }

    Toastify({
        text: evt.detail.message,
        duration: 3000,
        gravity: "top",
        position: "right",
        style: {
            background: bgColor,
            color: textColor
        }
    }).showToast();
    
});


function initTomSelect(el, item) {
    let ts = new TomSelect(el, {
        valueField: 'itemId',
        labelField: 'name',
        searchField: ['name'],
        preload: false,
        persist: false,
        maxOptions: 50,
        load: function (query, callback) {
            if (!query.length) {
                callback();
                return;
            }


            var url = '/items/api/list?q=' + encodeURIComponent(query);
            fetch(url)
                .then(response => response.json())
                .then(json => { this.clearOptions(); callback(json); })
                .catch(() => { callback(); });
        },
        render: {
            option: function (item, escape) {
                return '<div>' + escape(item.name) + '</div>';
            }
        }
    })
}