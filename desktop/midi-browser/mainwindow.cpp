/****************************************************************************
**
** Copyright (C) 2013 Digia Plc and/or its subsidiary(-ies).
** Contact: http://www.qt-project.org/legal
**
** This file is part of the examples of the Qt Toolkit.
**
** $QT_BEGIN_LICENSE:BSD$
** You may use this file under the terms of the BSD license as follows:
**
** "Redistribution and use in source and binary forms, with or without
** modification, are permitted provided that the following conditions are
** met:
**   * Redistributions of source code must retain the above copyright
**     notice, this list of conditions and the following disclaimer.
**   * Redistributions in binary form must reproduce the above copyright
**     notice, this list of conditions and the following disclaimer in
**     the documentation and/or other materials provided with the
**     distribution.
**   * Neither the name of Digia Plc and its Subsidiary(-ies) nor the names
**     of its contributors may be used to endorse or promote products derived
**     from this software without specific prior written permission.
**
**
** THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
** "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
** LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
** A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
** OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
** SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
** LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
** DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
** THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
** (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
** OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE."
**
** $QT_END_LICENSE$
**
****************************************************************************/

#include <QtWidgets>
#include <QtNetwork>
#include <QtWebKitWidgets>
#include <QDebug>
#include <QNetworkAccessManager>
#include <QNetworkRequest>
#include <QNetworkReply>

#include "mainwindow.h"
#include "midilib_mac.h"

//! [1]


MainWindow::MainWindow()
{
//    QUrl url("http://rick-li.github.com/android-midi/test.html");
    QUrl url("http://www.duosuccess.com");
    progress = 0;

    QString tmppath = QDir::tempPath();
    qDebug() << "Tmp path is " << tmppath;
    midiFilePath = tmppath + QDir::separator() + "tmp.mid";

    QNetworkProxyFactory::setUseSystemConfiguration(true);
    view = new QWebView(this);

    view -> load(url);

    connect(&manager, SIGNAL(finished(QNetworkReply*)),
            SLOT(downloadFinished(QNetworkReply*)));

    connect(view, SIGNAL(loadFinished(bool)), SLOT(adjustLocation()));
    connect(view, SIGNAL(titleChanged(QString)), SLOT(adjustTitle()));
    connect(view, SIGNAL(loadProgress(int)), SLOT(setProgress(int)));
    connect(view, SIGNAL(loadFinished(bool)), SLOT(finishLoading(bool)));

    locationEdit = new QLineEdit(this);
    locationEdit->setSizePolicy(QSizePolicy::Expanding, locationEdit->sizePolicy().verticalPolicy());
    connect(locationEdit, SIGNAL(returnPressed()), SLOT(changeLocation()));

    QToolBar *toolBar = addToolBar(tr("Navigation"));
    toolBar->addAction(view->pageAction(QWebPage::Back));
    toolBar->addAction(view->pageAction(QWebPage::Forward));
    toolBar->addAction(view->pageAction(QWebPage::Reload));
    stopAction = view->pageAction(QWebPage::Stop);
    connect(stopAction, SIGNAL(triggered()), this, SLOT(stopMusic()));

    toolBar->addAction(stopAction);
    toolBar->addWidget(locationEdit);
    setCentralWidget(view);
    setUnifiedTitleAndToolBarOnMac(true);
}
//! [4]
void MainWindow::adjustLocation()
{
    locationEdit->setText(view->url().toString());
}

void MainWindow::changeLocation()
{
    QUrl url = QUrl::fromUserInput(locationEdit->text());
    view->load(url);
    view->setFocus();
}
//! [4]

//! [5]
void MainWindow::adjustTitle()
{
    if (progress <= 0 || progress >= 100)
        setWindowTitle(view->title());
    else
        setWindowTitle(QString("%1 (%2%)").arg(view->title()).arg(progress));
}

void MainWindow::setProgress(int p)
{
    progress = p;
    adjustTitle();
}
//! [5]

//! [6]
void MainWindow::finishLoading(bool)
{
    progress = 100;
    adjustTitle();
    view->page()->mainFrame()->evaluateJavaScript("window.stopmusic = function(){}");
    QString sMidUrl = view->page()->mainFrame()->evaluateJavaScript("document.querySelector('embed').src").toString();
    qDebug() << sMidUrl;
    //webView.loadUrl("javascript:window.stopmusic = function(){}");
    //webView.loadUrl("javascript:midiExtractor.extract(document.querySelector('embed').src, window.location.href);");
    QUrl midurl(sMidUrl);
    QNetworkRequest request(midurl);
    QNetworkReply *reply = manager.get(request);

}

void MainWindow::downloadFinished(QNetworkReply *reply)
{
    //save the file to tmp dir
    QFile tmpMidFile(midiFilePath);

    if (!tmpMidFile.open(QIODevice::WriteOnly)) {
        fprintf(stderr, "Could not open %s for writing: %s\n",
                qPrintable(midiFilePath),
                qPrintable(tmpMidFile.errorString()));
        return;
    }

    tmpMidFile.write(reply->readAll());
    tmpMidFile.close();
    qDebug() << "File " << midiFilePath << " is written to disk.";

    midilib.play(&midiFilePath);
    stopAction->setEnabled(true);
//    QTimer::singleShot(1000 * 5, this, SLOT(stopMusic()));
    QTimer::singleShot(1000 * 60 * 60, this, SLOT(stopMusic()));
}


void MainWindow::clearCache(){
    QFile tmpMidFile(midiFilePath);
    if(tmpMidFile.exists() == true){
        bool status = tmpMidFile.remove();
        qDebug() << midiFilePath << " Removed status " << status;
    }
}

void MainWindow::stopMusic(){
    qDebug() << "Stopping Music ";
    midilib.stop();
    clearCache();
}
MainWindow::~MainWindow(){
    stopMusic();
}
