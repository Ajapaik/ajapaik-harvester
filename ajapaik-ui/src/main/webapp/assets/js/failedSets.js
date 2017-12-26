function showFailedSets() {
    request("getFailedSets", {}, function (response) {
        Object.keys(response).sort().reverse().forEach(function (date) {
            var $list = $('#failed-sets').append('<h4>'+date+'</h4>').append('<ul></ul>')
            response[date].forEach(function (set) {
                $list.append('<li>'+set+'</li>')
            })
        })
    })
}

