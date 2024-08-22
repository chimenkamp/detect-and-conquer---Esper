#set($eventKeys = $eventsMap.keySet().toArray())
#set($eventValues = $eventsMap.values().toArray())
#set($eventCount = $eventKeys.size() - 1)

#set($eventValuesList = '')
#foreach($value in $eventValues)
    #if($foreach.count > 1)
        #set($eventValuesList = "$eventValuesList, '$value'")
    #else
        #set($eventValuesList = "'$value'")
    #end
#end

SELECT * FROM PATTERN [
    EVERY
    #foreach($i in [0..$eventCount])
        #set($prevIndex = $i - 1)
        #set($previousEvent = $eventKeys[$prevIndex])
        #set($currentEvent = $eventKeys[$i])
        ($eventKeys[$i] = $EPPMEventType(activity ='$eventValues[$i]'))
        #if($i > 0)
        -> NOT $EPPMEventType(
            timestamp > ${previousEvent}.timestamp AND
            timestamp <  ${currentEvent}.timestamp AND
            activity NOT IN ($eventValuesList) AND
            caseID = ${previousEvent}.caseID
        )
        #end
        #if($i < $eventCount)
        ->
        #end
    #end
].win:time($time_window)
