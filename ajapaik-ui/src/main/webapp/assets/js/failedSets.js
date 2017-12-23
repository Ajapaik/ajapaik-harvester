function showFailedSets() {
    request("getFailedSets", {}, function (response) {
        Object.keys(response).forEach(function (date) {
            $('#failed-sets').append('<h4>'+date+'</h4>').append('<ul></ul>')
            response[date].forEach(function (set) {
                $('#failed-sets ul').append('<li>'+set+'</li>')
            })
        })
    })
}

