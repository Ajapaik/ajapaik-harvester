var infosystems = {}

$(function () {
    $('#schedule').datetimepicker({
        sideBySide: true,
        format: 'YYYY-MM-DD HH:mm'
    })

    $('#lastHarvestDate').datetimepicker({
        format: 'YYYY-MM-DD'
    })

    $('#create-harvester').on('submit', function (e) {
        e.preventDefault()
        createInfoSystem()
    })

    $('#harvesters').on('change', function () {
        var selectedInfoSystem = this.value;
        var infoSystem = infosystems[selectedInfoSystem];
        if (infoSystem.lastHarvestTime) {
            $('#lastHarvestDate').datetimepicker('date', new Date(infoSystem.lastHarvestTime))
        }
    })
})

function loadInfosystems() {
    request('getNotCustomInfoSystems', {}, function (response) {
        response.forEach(function (is) {
            infosystems[is.name] = is
            $('#harvesters').append('<option value="' + is.name + '">' + is.name + '</option>')
        })
        $('#harvesters').trigger('change')
    })
}

function getSchedule() {
    var dateTime = $('#schedule').val()
    return '0 ' + dateTime.substr(14, 2) + ' ' + dateTime.substr(11, 2) + ' ' +
        dateTime.substr(8, 2) + ' ' + dateTime.substr(5, 2) + ' ? ' + dateTime.substr(0, 4)
}

function createInfoSystem() {
    var params = {}
    var setsToUse = $('#setsToUse').val()
    params['setsToUse'] = setsToUse ? $.map($('#setsToUse').val().split(','), $.trim).filter(function (set) { return !!set }) : []
    params['name'] = $('#harvesters').val()
    params['lastHarvestTime'] = $('#lastHarvestDate').val()
    params['schedule'] = getSchedule()
    request('initCustomHarvester', [params], function () {
        $('#harvester-view alert').show()
    })
}