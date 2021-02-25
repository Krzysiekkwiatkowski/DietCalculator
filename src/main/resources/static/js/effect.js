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

    $(window).on('resize', function(){
        resizeContent();
    });

    function resizeContent(){
        document.body.innerHTML = document.body.innerHTML.replace("\n", "");
        var prefContentWidth = 984;
        var prefContentHeight = 174;
        var actualWidth = $(window).width();
        var actualHeight = $(window).height() - 22;
        if(actualWidth > prefContentWidth){
            var numberToSet = Math.floor((actualWidth - prefContentWidth) / 2) - 8;
            $('#leftPanel').css('width', numberToSet + 'px');
            $('#rightPanel').css('width', numberToSet + 'px');
            $('#content').css('width', prefContentWidth + 'px');
        }
        if(actualHeight > prefContentHeight) {
            $('#leftPanel').css('height', actualHeight + 'px');
            $('#rightPanel').css('height', actualHeight + 'px');
            $('#content').css('height', actualHeight + 'px');
        }
    }

    resizeContent();

    var settingContent = $('.settingContent');
    var selfDistributionCheckbox = $('#selfDistribution');

    selfDistributionCheckbox.on('change', function(){
        console.log("kliknięcie");
        updateView($(this).is(':checked'))
    });

    function updateView(checked){
        if(checked){
            settingContent.css('display', '');
        } else {
            settingContent.css('display' , 'none');
        }
    }

    updateView(selfDistributionCheckbox.is(':checked'));
});