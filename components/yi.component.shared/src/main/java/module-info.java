module yi.component.shared {
    requires org.jetbrains.annotations;
    requires javafx.media;
    requires javafx.controls;
    requires yi.core;
    requires json;

    exports yi.component.shared;
    exports yi.component.shared.audio;
    exports yi.component.shared.component;
    exports yi.component.shared.component.modal;
    exports yi.component.shared.i18n;
    exports yi.component.shared.utilities;
    exports yi.component.shared.property;
}