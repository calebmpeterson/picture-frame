(function($) {
	var imgList = [];
	$.extend({
		preload: function(imgArr, option) {
			var setting = $.extend({
				init: function(loaded, total) {},
				loaded: function(img, loaded, total) {},
				loaded_all: function(loaded, total) {}
			}, option);
			var total = imgArr.length;
			var loaded = 0;

			setting.init(0, total);
			for(var i in imgArr) {
				imgList.push($("<img />")
					.attr("src", imgArr[i])
					.load(function() {
						loaded++;
						setting.loaded(this, loaded, total);
						if(loaded == total) {
							setting.loaded_all(loaded, total);
						}
					})
				);
			}

		}
	});
})(jQuery);

$(function () {

    function updatePhoto() {
        $.ajax({
            url: "/next",
            success: function (path) {
                console.log('photo path:', path);

                $.preload(['/picture/' + path, '/blur/' + path],
                         {
                           loaded_all: updatePhotos
                         });

                function updatePhotos() {
                  var photoUrl = 'url("/picture/' + path + '")',
                      blurUrl  = 'url("/blur/' + path + '")';

                  console.log('photo loaded:', path);
                  console.log(' photo:', photoUrl);
                  console.log(' blur: ', blurUrl);

                  $('.photo-frame').css({ 'background-image' : blurUrl });
                  $('.photo').css({ 'background-image' : photoUrl });
                }
            },
            error: function () {
                console.log(arguments);
            }
        });
    }

    setInterval(updatePhoto, 30000);

    updatePhoto();
});

$(function () {
    function updateWeather() {
        $.getJSON('/weather',
            function (result) {
                console.log(result);
                $('#weather #high').html(result.high + '&deg;');
                $('#weather #low').html(result.low + '&deg;');
                //$('#weather-icon').attr('src', result.icon_url);
            });
    }

    setInterval(updateWeather, 60 * 60 * 1000);

    updateWeather();
});

$(function () {
    function updateClock() {
        $('#time').html(moment().format("h:mm A"));
        $('#date').html(moment().format("MMMM Do YYYY"));
    }

    setInterval(updateClock, 100);
});
