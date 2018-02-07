#!/usr/bin/env bash

lower()
{
    tr '[:upper:]' '[:lower:]'
}
truth_echo()
{
    if truth "$1"; then
        [[ $2 ]] && echo "$2"
    else
        [[ $3 ]] && echo "$3"
    fi
}
truth_value()
{
    truth_echo "$1" 1 0
}

truth()
{
    case $(lower <<<"$1") in
        yes|y|true|t|on|1) return 0 ;;
    esac
    return 1
}

interactive()
{
    if (( $# == 0 )); then
        truth $INTERACTIVE && return 0
        return 1
    fi

    export INTERACTIVE=$(truth_value $1)
}

choice()
{
    local p="Select from these choices"
    local d
    local c

    unset OPTIND
    while getopts ":p:d:c" option; do
        case $option in
            p) p=$OPTARG ;;
            d) d=$OPTARG ;;
            c) c=1 ;;
        esac
    done && shift $(($OPTIND - 1))

    if truth $c && ! interactive; then
        return
    fi

    if (( $# <= 1 )); then
        echo "$1"
        exit
    fi

    echo "$p:" >&2
    select choice in "$@"; do
        if [ "$choice" = "No thank you" ]; then
            echo ""
            break
        elif [[ $choice ]]; then
            echo "-D$d=$choice "
            break
        fi
    done
}

interactive y

ARGS=""
ARGS+=$(choice -p "Which web server component do you use?" \
               -d "webServer"\
               "undertow" "jetty" "No thank you")
ARGS+=$(choice -p "Which template engine component do you use?" \
               -d "template" \
               "freemarker" "thymeleaf" "No thank you")
ARGS+=$(choice -p "Which data source component do you use?" \
               -d "datasource" \
               "HikariCP" "No thank you")
ARGS+=$(choice -p "Which migration component do you use?" \
               -d "migration" \
               "flyway" "No thank you")
ARGS+=$(choice -p "Which O/R mapper component do you use?" \
               -d "ORMapper" \
               "doma2" "No thank you")

echo $ARGS

mvn -e archetype:generate \
    -DarchetypeGroupId=net.unit8.enkan \
    -DarchetypeArtifactId=kotowari-archetype \
    -DarchetypeVersion=0.6.0-SNAPSHOT \
    -DarchetypeCatalog=internal $ARGS
