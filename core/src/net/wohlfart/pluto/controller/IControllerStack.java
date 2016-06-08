package net.wohlfart.pluto.controller;

import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Matrix4;

public interface IControllerStack extends InputProcessor {

    Matrix4 calculateTransform(long now, float delta);

    CamRobotInput getRobotInput();

    CommandInput getCommandInput();

}
