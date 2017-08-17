# User Manual

## Running the application

When you execute the application, it will try to load a configuration file called config.json located in doc/examples/.

You can specify a config file using the using the -f flag, for example 'java -jar app.jar -f doc/examples/config1.json'

## The config file

The teams/boats are specified in the config file under 'teams', each team requires a team name, and a currentVelocity (in meters per second).

The 'time-scale' option lets you change how long the race takes to complete. A time-scale of 1.0 is normal speed, 2.0 is 2x etc.

The 'race-size' option lets you specify how many boats will be selected to compete in each race. There must be at least this many teams defined.