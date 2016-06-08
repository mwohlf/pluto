package net.wohlfart.pluto.gui;

import aurelienribon.tweenengine.Timeline;

interface ILayout {

    Timeline show(IWidgetContainer container);

    Timeline hide(IWidgetContainer container);

    Timeline refresh(IWidgetContainer container);

}
