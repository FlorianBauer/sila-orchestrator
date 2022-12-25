#!/bin/bash

# Resolve the location of the sila-orchestrator installation.
# This includes resolving any symlinks.
PRG=$0
while [ -h "$PRG" ]; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '^.*-> \(.*\)$' 2>/dev/null`
    if expr "$link" : '^/' 2> /dev/null >/dev/null; then
        PRG="$link"
    else
        PRG="`dirname "$PRG"`/$link"
    fi
done

ORCHESTRATOR_DIR="`dirname "$PRG"`"

# absolutize dir
oldpwd=`pwd`
cd "${ORCHESTRATOR_DIR}"
cd ".."
ORCHESTRATOR_DIR=`pwd`
cd "${oldpwd}"

ICON_NAME=sila-orchestrator-icon
TMP_DIR=`mktemp --directory`
DESKTOP_FILE="$TMP_DIR/sila-orchestrator.desktop"
cat << EOF > $DESKTOP_FILE

[Desktop Entry]
Version=1.0
Encoding=UTF-8
Name=sila-orchestrator
Keywords=sila
Comment=A SiLA 2 complaint client.
Type=Application
Categories=Development
Terminal=false
StartupWMClass=sila-orchestator
Exec=java -jar "$ORCHESTRATOR_DIR/target/sila-orchestrator.jar"
MimeType=x-scheme-handler/sila-orchestrator;
Icon=$ORCHESTRATOR_DIR/doc/pictures/$ICON_NAME.svg
EOF

# seems necessary to refresh immediately:
chmod 644 $DESKTOP_FILE

xdg-desktop-menu install $DESKTOP_FILE
xdg-icon-resource install --size 128 "$ORCHESTRATOR_DIR/doc/pictures/sila-orchestrator-logo-128px.png" $ICON_NAME

rm $DESKTOP_FILE
rm -rf $TMP_DIR
