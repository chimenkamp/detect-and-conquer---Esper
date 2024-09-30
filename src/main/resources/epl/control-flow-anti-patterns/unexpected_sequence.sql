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
    #if($eventCount == 0)
    #set($singleEvent = $eventKeys[0])
    (
        $singleEvent = ubt.process_analytics.esper.EPPMEventType(activity ='$eventValues[0]')
        -> ubt.process_analytics.esper.EPPMEventType(
            timestamp > $singleEvent.timestamp AND
            activity != '$eventValues[0]' AND
            caseID = $singleEvent.caseID
        )
    )
    #else
    #foreach($i in [0..$eventCount])
       #set($currentEvent = $eventKeys[$i])

        #if($i < $eventCount)
            #set($nextIndex = $i + 1)
            #set($nextEvent = $eventKeys[$nextIndex])

            (
                $eventKeys[$i] = ubt.process_analytics.esper.EPPMEventType(activity ='$eventValues[$i]')
                -> ubt.process_analytics.esper.EPPMEventType(
                    timestamp > ${currentEvent}.timestamp AND
                    activity != '$eventValues[$nextIndex]' AND
                    caseID = ${currentEvent}.caseID
                )
            )
        #else
            (
                $eventKeys[$i] = ubt.process_analytics.esper.EPPMEventType(activity ='$eventValues[$i]')
            )
            #end

            #if($i < $eventCount)
                OR
            #end
    #end
#end
)].win:time($time_window)
