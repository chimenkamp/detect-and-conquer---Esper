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
    (
        #foreach($i in [0..$eventCount])
            #set($prevIndex = $i - 1)
            #set($previousEvent = $eventKeys[$prevIndex])
            #set($currentEvent = $eventKeys[$i])

            #if($i > 0)
            (
            $eventKeys[$prevIndex] = $EPPMEventType(activity ='$eventValues[$prevIndex]')
            -> NOT $EPPMEventType(
                timestamp > ${previousEvent}.timestamp AND
                activity IN ($eventValuesList) AND
                caseID = ${previousEvent}.caseID
                )
            )
        OR
        #end

        (
            $eventKeys[$i] = $EPPMEventType(activity ='$eventValues[$i]')
            -> NOT $EPPMEventType(
                timestamp > ${currentEvent}.timestamp AND
                activity IN ($eventValuesList) AND
                caseID = ${currentEvent}.caseID
            )
        )

    #if($i < $eventCount)
    OR
    #end
    #end
    )
    ].win:time(50 sec)
