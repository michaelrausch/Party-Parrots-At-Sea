# Design Decisions
- Code structure

App creates a race instance which can:
instantiate a file parser to extract race setting and team information;
creates and passes legs and teams/boats into event generator to create events;
runs a race and iterates all events that returned from the generator;
prints out event details, including time, involved boats and legs.

- Configuration file

We decided to store the team information including team names and boat currentVelocity, as well as race configuration setting in external file.

To read external files, "Json-simple" library has been used to parse information. 
By using this library, we did not have to write our json parser and benefited from the flexibility of json files.

