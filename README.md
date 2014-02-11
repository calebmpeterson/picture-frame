# picture-frame

A simple browser-based digital photo frame app. A date/time display and daily forecast display are included but currently disabled.

## Prerequisites

You will need [Leiningen][1] 1.7.0 or above installed.

[1]: https://github.com/technomancy/leiningen


## Configuration

Place the following in `config.conf`

	{:photo-library "absolute-path-to-your-photo-library"
 	 :wunderground-api-key "your-wunderground-api-key"
 	 :weather-location "location-for-weather-forecasts"}


## Running

To start a web server for the application using [Leiningen](http://leiningen.org), run:

    lein ring server

When uberjar'ed:

  java -Dconfig=config.conf -Xmx512M -cp target/picture-frame-standalone.jar clojure.main -m picture-frame.launcher 3282



## License

Copyright &copy; 2014 Caleb Peterson
