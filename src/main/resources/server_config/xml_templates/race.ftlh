<?xml version="1.0" encoding="utf-8"?>
<Race>
    <CreationTimeDate>${raceStartTime}</CreationTimeDate>
    <RaceStartTime Start="${raceStartTime}" Postpone="False" />
    <RaceID>15082901</RaceID>
    <RaceType>Fleet</RaceType>

    <Participants>
        <#list boats as boat>
            <Yacht SourceID="${boat.sourceId}"/>
        </#list>
    </Participants>

    <Course>
        <CompoundMark CompoundMarkID="1" Name="Mark0">
            <Mark SeqID="1" Name="Start Line 1" TargetLat="57.670603" TargetLng="11.828262" SourceID="122" />
            <Mark SeqID="2" Name="Start Line 2" TargetLat="57.669445" TargetLng="11.826413" SourceID="123" />
        </CompoundMark>
        <CompoundMark CompoundMarkID="2" Name="Mark1">
            <Mark SeqID="1" Name="Mark1" TargetLat="57.6675700" TargetLng="11.8359880" SourceID="131" />
        </CompoundMark>
        <CompoundMark CompoundMarkID="3" Name="Mark2">
            <Mark SeqID="1" Name="Lee Gate 1" TargetLat="57.6708220" TargetLng="11.8433900" SourceID="124" />
            <Mark SeqID="2" Name="Lee Gate 2" TargetLat="57.671629" TargetLng="11.840951" SourceID="125" />
        </CompoundMark>
        <CompoundMark CompoundMarkID="4" Name="Mark3">
            <Mark SeqID="1" Name="Wind Gate 1" TargetLat="57.665316" TargetLng="11.827184" SourceID="126" />
            <Mark SeqID="2" Name="Wind Gate 2" TargetLat="57.664190" TargetLng="11.829576" SourceID="127" />
        </CompoundMark>
        <CompoundMark CompoundMarkID="5" Name="Mark4">
            <Mark SeqID="1" Name="Finish Line 1" TargetLat="57.672350" TargetLng="11.842535" SourceID="128" />
            <Mark SeqID="2" Name="Finish Line 2" TargetLat="57.6715240" TargetLng="11.8444950" SourceID="129" />
        </CompoundMark>
    </Course>
    <CompoundMarkSequence>
        <Corner SeqID="1" CompoundMarkID="1" Rounding="PS" ZoneSize="3" />
        <Corner SeqID="2" CompoundMarkID="2" Rounding="Port" ZoneSize="3" />
        <Corner SeqID="3" CompoundMarkID="3" Rounding="SP" ZoneSize="3" />
        <Corner SeqID="4" CompoundMarkID="4" Rounding="PS" ZoneSize="3" />
        <Corner SeqID="5" CompoundMarkID="3" Rounding="SP" ZoneSize="3" />
        <Corner SeqID="6" CompoundMarkID="4" Rounding="PS" ZoneSize="3" />
        <Corner SeqID="7" CompoundMarkID="3" Rounding="SP" ZoneSize="3" />
        <Corner SeqID="8" CompoundMarkID="4" Rounding="PS" ZoneSize="3" />
        <Corner SeqID="9" CompoundMarkID="3" Rounding="SP" ZoneSize="3" />
        <Corner SeqID="10" CompoundMarkID="4" Rounding="PS" ZoneSize="3" />
        <Corner SeqID="11" CompoundMarkID="5" Rounding="PS" ZoneSize="3" />
    </CompoundMarkSequence>
    <CourseLimit>
        <Limit SeqID="1" Lat="57.6739450" Lon="11.8417100" />
        <Limit SeqID="2" Lat="57.6709520" Lon="11.8485010" />
        <Limit SeqID="3" Lat="57.6690260" Lon="11.8472790" />
        <Limit SeqID="4" Lat="57.6693140" Lon="11.8457610" />
        <Limit SeqID="5" Lat="57.6665370" Lon="11.8432910" />
        <Limit SeqID="6" Lat="57.6641400" Lon="11.8385840" />
        <Limit SeqID="7" Lat="57.6629430" Lon="11.8332030" />
        <Limit SeqID="8" Lat="57.6629480" Lon="11.8249660" />
        <Limit SeqID="9" Lat="57.6686890" Lon="11.8250920" />
        <Limit SeqID="10" Lat="57.6692230" Lon="11.8231430" />
        <Limit SeqID="11" Lat="57.6725370" Lon="11.8272480" />
        <Limit SeqID="12" Lat="57.6708220" Lon="11.8321340" />
    </CourseLimit>
</Race>