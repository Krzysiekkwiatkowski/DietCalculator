$(function () {
    var buttons = $('button');
    buttons.on('mouseenter', function () {
        $(this).css('backgroundColor', 'dimgrey');
    });

    buttons.on('mouseleave', function () {
        $(this).css('backgroundColor', 'grey')
    });

    var links = $('a');
    links.on('mouseenter', function () {
        $(this).css('color', 'darkblue');
    });

    links.on('mouseleave', function () {
        $(this).css('color', 'darkcyan');
    });

    var inputs = $('input');
    inputs.on('focus', function () {
        $(this).css('backgroundColor', 'whitesmoke');
        $(this).css('border', '2px solid black')
    });

    inputs.on('blur', function () {
        $(this).css('backgroundColor', 'lightgrey');
        $(this).css('border','')
    });

    var selects = $('select');
    selects.on('focus', function () {
        $(this).css('backgroundColor', 'whitesmoke');
        $(this).css('border', '2px solid black')
    });

    selects.on('blur', function () {
        $(this).css('backgroundColor', 'lightgrey');
        $(this).css('border','')
    });

    var deleteLinks = $('.delete');
    deleteLinks.on('click', function (event) {
        $(this).next().css('display', 'contents');
        event.preventDefault();
    });

    var settingContent = $('#settingContent');
    var selfDistributionCheckbox = $('#selfDistribution');

    selfDistributionCheckbox.on('change', function(){
        updateView($(this).is(':checked'))
    });

    function updateView(checked){
        if(checked){
            settingContent.css('display', '');
        } else {
            settingContent.css('display' , 'none');
        }
    }

    updateView(selfDistributionCheckbox.is(':checked'))
});