#!/bin/sh

MARIADB_VERSION=11.3.2-linux-systemd-x86_64
mode=$1
MARIADB_HOME=/opt/mariadb/mariadb-${MARIADB_VERSION}
basedir=$MARIADB_HOME
bindir=$basedir/bin
mariadbd_pid_file_path=$basedir/data/`hostname`.pid
service_startup_timeout=900
# Lock directory for RedHat / SuSE.
lockdir='/var/lock/subsys'
lock_file_path="$lockdir/mysql"
other_args="$*"

  log_success_msg()
  {
    echo " SUCCESS! $@"
  }
  log_failure_msg()
  {
    echo " ERROR! $@"
  }

wait_for_pid () {
  verb="$1"           # created | removed
  pid="$2"            # process ID of the program operating on the pid-file
  pid_file_path="$3" # path to the PID file.

  i=0
  avoid_race_condition="by checking again"

  while test $i -ne $service_startup_timeout ; do

    case "$verb" in
      'created')
        # wait for a PID-file to pop into existence.
        test -s "$pid_file_path" && i='' && break
        ;;
      'removed')
        # wait for this PID-file to disappear
        test ! -s "$pid_file_path" && i='' && break
        ;;
      *)
        echo "wait_for_pid () usage: wait_for_pid created|removed pid pid_file_path"
        exit 1
        ;;
    esac

    # if server isn't running, then pid-file will never be updated
    if test -n "$pid"; then
      if kill -0 "$pid" 2>/dev/null; then
        :  # the server still runs
      else
        # The server may have exited between the last pid-file check and now.
        if test -n "$avoid_race_condition"; then
          avoid_race_condition=""
          continue  # Check again.
        fi

        # there's nothing that will affect the file.
        log_failure_msg "The server quit without updating PID file ($pid_file_path)."
        return 1  # not waiting any more.
      fi
    fi

    echo $echo_n ".$echo_c"
    i=`expr $i + 1`
    sleep 1

  done

  if test -z "$i" ; then
    log_success_msg
    return 0
  else
    log_failure_msg
    return 1
  fi
}

case "$mode" in
  'start')
    # Start daemon

    # Safeguard (relative paths, core dumps..)
    cd $basedir

    echo $echo_n "Starting Mariadb"
    if test -x $bindir/mariadbd-safe
    then
      # Give extra arguments to mysqld with the my.cnf file. This script
      # may be overwritten at next upgrade.
      $bindir/mariadbd-safe --defaults-file=${MARIADB_HOME}/my.cnf --pid-file="$mariadbd_pid_file_path" --user=mysql>/dev/null &
      wait_for_pid created "$!" "$mariadbd_pid_file_path"; return_value=$?

      # Make lock for RedHat / SuSE
      if test -w "$lockdir"
      then
        touch "$lock_file_path"
      fi

      exit $return_value
    else
      log_failure_msg "Couldn't find Mariadb server ($bindir/mariadbd-safe)"
    fi
    ;;

  'stop')
    # Stop daemon. We use a signal here to avoid having to know the
    # root password.

    if test -s "$mariadbd_pid_file_path"
    then
      # signal mariadbdd_safe that it needs to stop
      touch "$mariadbd_pid_file_path.shutdown"
      mysqld_pid=`cat "$mariadbd_pid_file_path"`

      if (kill -0 $mysqld_pid 2>/dev/null)
      then
        echo $echo_n "Shutting down Mariadb"
        kill $mysqld_pid
        # mysqld should remove the pid file when it exits, so wait for it.
        wait_for_pid removed "$mysqld_pid" "$mariadbd_pid_file_path"; return_value=$?
      else
        log_failure_msg "Mariadb server process #$mysqld_pid is not running!"
        rm "$mariadbd_pid_file_path"
      fi

      # Delete lock for RedHat / SuSE
      if test -f "$lock_file_path"
      then
        rm -f "$lock_file_path"
      fi
      exit $return_value
    else
      log_failure_msg "Mariadb server PID file could not be found!"
    fi
    ;;

  'restart')
    # Stop the service and regardless of whether it was
    # running or not, start it again.
    if $0 stop  $other_args; then
      $0 start $other_args
    else
      log_failure_msg "Failed to stop running server, so refusing to try to start."
      exit 1
    fi
    ;;

  'reload'|'force-reload')
    if test -s "$mariadbd_pid_file_path" ; then
      read mysqld_pid <  "$mariadbd_pid_file_path"
      kill -HUP $mysqld_pid && log_success_msg "Reloading service Mariadb"
      touch "$mariadbd_pid_file_path"
    else
      log_failure_msg "Mariadb PID file could not be found!"
      exit 1
    fi
    ;;
  'status')
    # First, check to see if pid file exists
    if test -s "$mariadbd_pid_file_path" ; then
      read mysqld_pid < "$mariadbd_pid_file_path"
      if kill -0 $mysqld_pid 2>/dev/null ; then
        log_success_msg "Mariadb running ($mysqld_pid)"
        exit 0
      else
        log_failure_msg "Mariadb is not running, but PID file exists"
        exit 1
      fi
    else
      # Try to find appropriate mysqld process
      mysqld_pid=`pidof $libexecdir/mysqld`

      # test if multiple pids exist
      pid_count=`echo $mysqld_pid | wc -w`
      if test $pid_count -gt 1 ; then
        log_failure_msg "Multiple Mariadb running but PID file could not be found ($mysqld_pid)"
        exit 5
      elif test -z $mysqld_pid ; then
        if test -f "$lock_file_path" ; then
          log_failure_msg "Mariadb is not running, but lock file ($lock_file_path) exists"
          exit 2
        fi
        log_failure_msg "Mariadb is not running"
        exit 3
      else
        log_failure_msg "Mariadb is running but PID file could not be found"
        exit 4
      fi
    fi
    ;;
    *)
      # usage
      basename=`basename "$0"`
      echo "Usage: $basename  {start|stop|restart|reload|force-reload|status}  [ Mariadb server options ]"
      exit 1
    ;;
esac