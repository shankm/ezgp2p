
@echo off

if [%1]==[] GOTO Error 

SET seed=%1

@echo on

mkdir IAG_%seed%
    cd IAG_%seed%
    mkdir Incoming_%seed%
    cd Incoming_%seed%
        mkdir Archive_%seed%
        mkdir Error_%seed%
        mkdir temp_%seed%
        cd ..
    mkdir Outgoing_%seed%
        cd Outgoing_%seed%
        mkdir Archive_%seed%
        mkdir Error_%seed%
        cd ..
    mkdir Torrent_%seed%
        cd Torrent_%seed%
        mkdir Incoming_%seed%
        mkdir Outgoing_%seed%
        mkdir SharedFiles_%seed%
        cd ..
    cd ..

    @echo off
    GOTO Success

:Error
echo You must provide a seed.
echo (Ex: "%0 2" or "%0 two")

:Success
