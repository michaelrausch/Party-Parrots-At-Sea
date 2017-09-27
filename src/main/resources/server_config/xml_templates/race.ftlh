<?xml version="1.0" encoding="utf-8"?>
<Race>
    <CreationTimeDate>${raceStartTime}</CreationTimeDate>
    <RaceStartTime Start="${raceStartTime}" Postpone="False" />
    <RaceID>15082901</RaceID>
    <RaceType>Fleet</RaceType>

    <Participants MaxPlayers="${maxPlayers}">
        <#list boats as boat>
            <Yacht SourceID="${boat.sourceId}"/>
        </#list>
    </Participants>

    <Tokens Enabled="${tokensEnabled}">
        <#list tokens as token>
            <Token TokenType="${token.tokenType}" TargetLat="${token.lat?c}" TargetLng="${token.lng?c}"/>
        </#list>
    </Tokens>

    <Course>
        <#list compoundMarks as compoundMark>
            <CompoundMark CompoundMarkID="${compoundMark.id?c}" Name="${compoundMark.name}">
            <#list compoundMark.marks as mark>
                <Mark SeqID="${mark.seqID?c}" Name="${mark.name}" TargetLat="${mark.lat?c}" TargetLng="${mark.lng?c}" SourceID="${mark.sourceID?c}" />
            </#list>
            </CompoundMark>
        </#list>
    </Course>

    <CompoundMarkSequence>
        <#list roundings as corner>
            <Corner SeqID="${corner.seqID?c}" CompoundMarkID="${corner.compoundMarkID?c}" Rounding="${corner.rounding}" ZoneSize="${corner.zoneSize?c}" />
        </#list>
    </CompoundMarkSequence>

    <CourseLimit>
        <#list courseLimit as limit>
            <Limit SeqID="${limit.seqID?c}" Lat="${limit.lat?c}" Lon="${limit.lng?c}" />
        </#list>
    </CourseLimit>

</Race>