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
});