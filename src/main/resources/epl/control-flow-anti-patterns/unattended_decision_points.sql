#set($positiveEvents = $positiveEventsMap.keySet().toArray())
#set($positiveEventValues = $positiveEventsMap.values().toArray())
#set($positiveEventCount = $positiveEvents.size() - 1)

#set($negativeEvents = $negativeEventsMap.keySet().toArray())
#set($negativeEventValues = $negativeEventsMap.values().toArray())
#set($negativeEventCount = $negativeEvents.size() - 1)

#set($positiveEventValuesList = '')
#foreach($value in $positiveEventValues)
    #if($foreach.count > 1)
        #set($positiveEventValuesList = "$positiveEventValuesList, '$value'")
    #else
        #set($positiveEventValuesList = "'$value'")
    #end
#end

#set($negativeEventValuesList = '')
#foreach($value in $negativeEventValues)
    #if($foreach.count > 1)
        #set($negativeEventValuesList = "$negativeEventValuesList, '$value'")
    #else
        #set($negativeEventValuesList = "'$value'")
    #end
#end

SELECT * FROM PATTERN [
    EVERY
    (
        #if($positiveEventCount == 0)
            #set($singlePositiveEvent = $positiveEvents[0])
            (
                $singlePositiveEvent = ubt.process_analytics.esper.EPPMEventType(activity ='$positiveEventValues[0]')
                -> NOT (
                    #if($negativeEventCount == 0)
                        ubt.process_analytics.esper.EPPMEventType(activity = '$negativeEventValues[0]')
                    #else
                        #foreach($i in [0..$negativeEventCount])
                            #set($currentNegativeEvent = $negativeEvents[$i])

                            ubt.process_analytics.esper.EPPMEventType(
                                timestamp > ${singlePositiveEvent}.timestamp AND
                        activity = '$negativeEventValues[$i]' AND
                        caseID = ${singlePositiveEvent}.caseID
                        )
                        #if($i < $negativeEventCount)
                            OR
                        #end
                    #end
                #end
                )
            )
        #else
            #foreach($i in [0..$positiveEventCount])
                #set($currentPositiveEvent = $positiveEvents[$i])

                (
                $positiveEvents[$i] = ubt.process_analytics.esper.EPPMEventType(activity ='$positiveEventValues[$i]')
                -> NOT (
                #if($negativeEventCount == 0)
                    ubt.process_analytics.esper.EPPMEventType(activity = '$negativeEventValues[0]')
                #else
                #foreach($j in [0..$negativeEventCount])
                    #set($currentNegativeEvent = $negativeEvents[$j])

                    ubt.process_analytics.esper.EPPMEventType(
                    timestamp > ${currentPositiveEvent}.timestamp AND
                    activity = '$negativeEventValues[$j]' AND
                    caseID = ${currentPositiveEvent}.caseID
                    )
                    #if($j < $negativeEventCount)
                        OR
                    #end
                #end
            #end
            )
        )
        #if($i < $positiveEventCount)
            OR
        #end
    #end
#end
)].win:time($time_window)
