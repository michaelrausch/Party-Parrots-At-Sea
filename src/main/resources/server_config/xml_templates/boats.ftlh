<?xml version="1.0" encoding="utf-8"?>

<BoatConfig>
    <Modified>2012-05-17T07:49:40+0200</Modified>
    <Version>12</Version>

    <Settings>
        <RaceBoatType Type="AC45" />
        <BoatDimension BoatLength="14.019" HullLength="13.449" />
        <ZoneSize MarkZoneSize="40.347" CourseZoneSize="40.347" />
        <ZoneLimits Limit1="200" Limit2="100" Limit3="40.347" Limit4="0" Limit5="-100" />
    </Settings>

    <BoatShapes>
        <#-- Not used -->
    </BoatShapes>

    <Boats>
        <#list boats as boat>
            <Boat Type="Yacht" SourceID="${boat.sourceId}" ShapeID="4" HullNum="${boat.hullId}" StoweName="${boat.shortName}" ShortName="${boat.shortName}"
                  BoatName="${boat.boatName}" Country="${boat.country}">

                <GPSposition Z="0" Y="${boat.lat}" X="${boat.lon}" />
                <MastTop Z="0" Y="${boat.lat}" X="${boat.lon}" />
            </Boat>
        </#list>
    </Boats>
</BoatConfig>