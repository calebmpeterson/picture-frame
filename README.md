# picture-frame

FIXME

## Prerequisites

You will need [Leiningen][1] 1.7.0 or above installed.

[1]: https://github.com/technomancy/leiningen


## Configuration

Place the following in `config.conf`

	{:photo-library "absolute-path-to-photo-library"
 	 :wunderground-api-key "your-wunderground-api-key"
 	 :weather-location "location-for-weather-forecasts"}
 	 

## Running

To start a web server for the application, run:

    lein ring server
    
    
Resize all images with [Image Resizer](https://imageresizer.codeplex.com/wikipage?title=User%27s%20Guide&referringTitle=Documentation)

## License

Copyright &copy; 2014 Caleb Peterson
