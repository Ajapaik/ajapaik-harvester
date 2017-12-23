$(function () {
    $('#create-harvester').on('submit', function (e) {
        e.preventDefault()
        createInfoSystem()
    })
})

function createInfoSystem() {
    var params = {}
    var setsToUse = $('#setsToUse').val()
    params['setsToUse'] = setsToUse ? $.map($('#setsToUse').val().split(","), $.trim).filter(function (set) { return !!set }) : []
    params['name'] = $('#harvesters').val()
    params['lastHarvestTime'] = $('#lastHarvestDate').val()
    params['schedule'] = $('#schedule').val()
    console.log(JSON.stringify(params))
    request('initCustomHarvester', [params], function () {
        $('#harvester-view alert').show()
    })
}