
// *********** pls include followings ****************************************
// *.html  :    <script type="text/javascript" src="../scripts/story.common.js"></script>
// *.scala :    import lib.{Quill, JCrop, Util, myChart, contextMenu}
// ************************************************************************


/* ------------------ Chart handler.js ------------------------------------------*/
// chart type : bar, pie, horizontalBar , polarArea , line
var label_data = ["Red", "Blue", "Yellow", "Green", "Purple", "Orange"];
var backColor = [
                    'rgba(255, 99, 132, 0.2)',
                    'rgba(54, 162, 235, 0.2)',
                    'rgba(255, 206, 86, 0.2)',
                    'rgba(75, 192, 192, 0.2)',
                    'rgba(153, 102, 255, 0.2)',
                    'rgba(75, 102, 132, 0.2)',
                    'rgba(255, 102, 235, 0.2)',
                    'rgba(54, 102, 86, 0.2)',
                    'rgba(255, 159, 192, 0.2)'
                ] ;
var bordColor =   [
                    'rgba(255,99,132,1)',
                    'rgba(54, 162, 235, 1)',
                    'rgba(255, 206, 86, 1)',
                    'rgba(75, 192, 192, 1)',
                    'rgba(153, 102, 255, 1)',
                    'rgba(153, 102, 255, 1)',
                    'rgba(153, 102, 255, 1)',
                    'rgba(153, 102, 255, 1)',
                    'rgba(255, 159, 64, 1)'
                ] ;


var chartOpt = {
        type: 'line',
        data: {
            labels: label_data,
            datasets: [{
                label: 'Your Result Graph',
                data: [2, 9, 3, 5, 2, 3],
                backgroundColor: backColor,
                borderColor: bordColor,
                borderWidth: 1
            }]
        },
        options: {
            scales: {
                yAxes: [{
                    ticks: {
                        beginAtZero:true
                    }
                }]
            }
        }
    }

function testChart(labels,datas) {
    chartOpt.data.labels = labels
    chartOpt.data.datasets[0].data = datas
    var ctx = document.getElementById("myChart");
    var myChart = new Chart(ctx,chartOpt );
}

function ResultChart(id, ctype,labels,datas, title) {

console.log(id, ctype,labels,datas)
    chartOpt.type = ctype
    chartOpt.type = ctype
    chartOpt.data.labels = labels
    chartOpt.data.datasets[0].data = datas
    chartOpt.data.datasets[0].label = title
    var ctx = document.getElementById(id);
console.log(ctx,chartOpt)

    var myChart = new Chart(ctx,chartOpt );

    return myChart
}

function UpdateChart(chart,id, ctype,labels,datas) {

console.log(id, ctype,labels,datas)
    chart.destroy();
    chartOpt.type = ctype
    chartOpt.data.labels = labels
    chartOpt.data.datasets[0].data = datas
    var ctx = document.getElementById(id);
    var myChart = new Chart(ctx,chartOpt );

    return myChart
}

function UpdateData(chart,id, labels,datas) {

    chart.destroy();
    //chartOpt.type = ctype
    chartOpt.data.labels = labels
    chartOpt.data.datasets[0].data = datas
    var ctx = document.getElementById(id);
    var myChart = new Chart(ctx,chartOpt );

    return myChart
}





// ******** jCrop.* Cropper Modal handling **************
var jcrop_api;
var awi_image_id ;  // image that is on awi-image
function toggleJCrop(orgImageId) {
    //remove old
    //new handler
    awi_image_id = orgImageId ; // save the awi-image id
    var x = document.getElementById("CropIt");
    if (x.className.indexOf("w3-show") == -1) {
        x.className += " w3-show";
        $('#theCropImage').Jcrop({
            onChange: updatePreview,
            onSelect: updatePreview,
            boxWidth: 600,
            allowSelect: true,
            allowMove: true,
            allowResize: true,
            aspectRatio: 0
	    },function(){
            jcrop_api = this;
        });
    } else {
        x.className = x.className.replace(" w3-show", "");
        if ($('#theCropImage').data('Jcrop')) {
            $('#theCropImage').data('Jcrop').destroy();
            $('#theCropImage').removeAttr('style');
        }
    }
}

var saved_size;
function updatePreview(c) {
    if (parseInt(c.w) > 0) {
        // Show image preview
        var imageObj = $("#theCropImage")[0];  // before image
        var canvas = $("#previewImage")[0];    // after image
        canvas.width = c.w;
        canvas.height = c.h;
        var context = canvas.getContext("2d");

        if (imageObj != null && c.x != 0 && c.y != 0 && c.w != 0 && c.h != 0) {
            // copy selected from orginal to after image
            context.drawImage(imageObj, c.x, c.y, c.w, c.h, 0, 0, canvas.width, canvas.height);
            saved_size = c ;
        }
    }
}

function completeJCrop() {
    var x = document.getElementById("CropIt");  // modal dialog

    var orgImage = document.getElementById(awi_image_id); //original awi-image
	var imageObj = $("#theCropImage")[0]; // before image copied from orgImage
	var canvas = $("#previewImage")[0];   // after imaged selected

	var context = canvas.getContext("2d");

	//var s = jcrop_api.tellScaled();
	var s = saved_size;
    canvas.width = s.w;
    canvas.height = s.h;

    if (imageObj != null && s.x != 0 && s.y != 0 && s.w != 0 && s.h != 0) {
  	   context.drawImage(imageObj, s.x,s.y,s.w, s.h, 0,0,canvas.width, canvas.height);
	   var dataURL = canvas.toDataURL("image/jpeg");
	   orgImage.src = dataURL;
    }
/*
    if (imageObj != null && s.x != 0 && s.y != 0 && s.w != 0 && s.h != 0) {
  	   context.drawImage(imageObj, s.x,s.y,s.w, s.h, 0,0,canvas.width, canvas.height);
	   var dataURL = canvas.toDataURL("image/jpeg");
	   orgImage.src = dataURL;
    }
*/
    x.className = x.className.replace(" w3-show", "");

    if ($('#theCropImage').data('Jcrop')) {
        $('#theCropImage').data('Jcrop').destroy();
        $('#theCropImage').removeAttr('style');
    }
}

// ******** Cropper Modal handling **************



// ******* gmail Function *****************************************************

var winRef = null;
function openGmail() {

      //winRef = dom.window.open(docBase + s"show-popup4.html?objId=$objId","SHOW_DIALOG","width=500;height=300;overflow-y=scroll")
      //winRef.focus()

      if (winRef != null ) winRef.close()
     winRef = window.open("gmail2.html", "_blank", "toolbar=yes,scrollbars=yes,resizable=yes,top=500,left=500,width=600,height=600");
     winRef.focus();
}

// ******* Util.* Function ***********

function guid() {
    function s4() {
      return ((1 + Math.random()) * 0x10000 | 0).toString(16).substring(1);
    }
    return s4() + s4() + '-' + s4() + '-' + s4() + '-' + s4() + '-' + s4() + s4() + s4();
}

function findAncestor (el, cls) {
    console.log("finding:" + cls );
    while ((el = el.parentElement) && !el.classList.contains(cls));
    console.log("found:" + cls );
    return el;
}