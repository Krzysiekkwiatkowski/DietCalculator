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
        updateView($(this).is(':checked'))
    });

    function hideProducts(){
        var elements = $('#categories').children();
        var products = $('#product').children();
        var categoryId = 0;
        for(var i = 0; i < elements.length; i++){
            if(elements[i].selected == true){
                categoryId = elements[i].value;
                break;
            }
        }

        var firstOption = true;
        for(var i = 0; i < products.length; i++){
            if(products[i].getAttribute('data') == categoryId){
                products[i].style.display = '';
                if(firstOption){
                    products[i].selected = true;
                    firstOption = false;
                }
            } else {
                products[i].style.display = 'none';
                if(products[i].selected == true){
                    products[i].selected == false;
                }
            }
        }
    }

    $('#categories').change(function(){
        hideProducts();
    });

    function updateView(checked){
        if(checked){
            settingContent.css('display', '');
        } else {
            settingContent.css('display' , 'none');
        }
    }

    var descriptions = document.getElementsByClassName('graphText');
    var graphs = document.getElementsByClassName('graph');
    var protein;
    var carbohydrates;
    var fat;
    var calories;
    var proteinGraph;
    var carbohydratesGraph;
    var fatGraph;
    var caloriesGraph;
    var glycemicChargeGraph;

    function initializeGraphs(){
        for (var i = 0; i < descriptions.length; i++){
            var text = descriptions[i].innerText.substring(0, (descriptions[i].innerText.indexOf("0/") + 1));
            switch(text){
                case "Białko: 0":
                    protein = descriptions[i];
                    break;
                case "Węglowodany: 0":
                    carbohydrates = descriptions[i];
                    break;
                case "Tłuszcz: 0":
                    fat = descriptions[i];
                    break;
                case "Kalorie: 0":
                    calories = descriptions[i];
                    break;
            }
        }
        for (var i = 0; i < graphs.length; i++){
            var text = graphs[i].parentElement.parentElement.parentElement.firstElementChild.innerHTML;
            var checkText = text.substring(0, text.indexOf(" "));
            switch(checkText){
                case "Białko:":
                    proteinGraph = graphs[i];
                    break;
                case "Węglowodany:":
                    carbohydratesGraph = graphs[i];
                    break;
                case "Tłuszcz:":
                    fatGraph = graphs[i];
                    break;
                case "Kalorie:":
                    caloriesGraph = graphs[i];
                    break;
                case "Ładunek":
                    glycemicChargeGraph = graphs[i];
                    break;
            }
        }
    }

    var sliders = document.getElementsByClassName('slider');
    if(sliders != null && sliders){
        for(var i = 0; i < sliders.length; i++) {
            sliders[i].oninput = function () {
                this.parentElement.nextElementSibling.innerHTML = parseInt(this.value) + 'g';
                sumValues();
            };
        }
    }

    function sumValues(){
        var proteinPart = 0;
        var carbohydratesPart = 0;
        var fatPart = 0;
        var caloriesPart = 0;
        var glycemicChargePart = 0;
        for(var i = 0; i < sliders.length; i++){
            var value = parseInt(sliders[i].value);
            var multiplier = value / 100;
            var product = sliders[i].parentElement.parentElement.firstElementChild;
            proteinPart += parseFloat(product.getAttribute('data-protein')) * multiplier;
            var actualCarbohydrates = parseFloat(product.getAttribute('data-carbohydrates')) * multiplier;
            carbohydratesPart += actualCarbohydrates;
            fatPart += parseFloat(product.getAttribute('data-fat')) * multiplier;
            caloriesPart += parseFloat(product.getAttribute('data-calories')) * multiplier;
            glycemicChargePart += (parseFloat(product.getAttribute('data-glycemicIndex')) * actualCarbohydrates)/ 100;
        }
        updateGraphs(proteinPart, carbohydratesPart, fatPart, caloriesPart, glycemicChargePart);
    }

    function updateGraphs(proteinPart, carbohydratesPart, fatPart, caloriesPart, glycemicChargePart){
        protein.innerText = getPreparedText(proteinGraph, protein.innerText, proteinPart);
        carbohydrates.innerText = getPreparedText(carbohydratesGraph, carbohydrates.innerText, carbohydratesPart);
        fat.innerText = getPreparedText(fatGraph, fat.innerText, fatPart);
        calories.innerText = getPreparedText(caloriesGraph, calories.innerText, caloriesPart);
        var width = parseInt((glycemicChargePart * 300) / 20);
        var graphText = width / 15;
        glycemicChargeGraph.innerText = roundNumberDisplay(graphText);
        glycemicChargeGraph.style.width = width + "px";
    }

    function getPreparedText(graph, text, value){
        var firstPart = text.substring(0, (text.indexOf(" ") + 1));
        var lastPart = (text.substring(text.indexOf("/"), text.length));
        setWidth(graph, value, text);
        return firstPart + roundNumberDisplay(value) + lastPart;
    }

    function getFullPart(text){
        return text.substring((text.indexOf("/") + 1), text.length);
    }

    function setWidth(graph, value, text){
        var newWidth = calculateWidth(value, text);
        graph.style.width = (newWidth * 3) + "px";
        graph.innerText = roundNumberDisplay(newWidth) + "%";
    }

    function calculateWidth(value, text){
        return parseInt(value * 100 )/ parseFloat(getFullPart(text));
    }

    function roundNumberDisplay(number){
        return Math.round(number * 10) / 10;
    }

    initializeGraphs();
    hideProducts();
    updateView(selfDistributionCheckbox.is(':checked'));
});