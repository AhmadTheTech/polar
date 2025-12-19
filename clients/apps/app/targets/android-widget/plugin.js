const { withInfoPlist, withAndroidQueries, withAndroidManifest, withProjectBuildGradle, withAppBuildGradle, withDangerousMod } = require('@expo/config-plugins');
const fs = require('fs');
const path = require('path');

module.exports = function withPolarAndroidWidget(config) {
    config = withAppBuildGradle(config, (config) => {
        if (config.modResults.language === 'groovy') {
            config.modResults.contents = addDependencies(config.modResults.contents);
        }
        return config;
    });

    config = withProjectBuildGradle(config, (config) => {
        if (config.modResults.language === 'groovy') {
            config.modResults.contents = addProjectPlugins(config.modResults.contents);
        }
        return config;
    });

    config = withAndroidManifest(config, (config) => {
        const mainApplication = config.modResults.manifest.application[0];

        if (!mainApplication.receiver) {
            mainApplication.receiver = [];
        }

        mainApplication.receiver.push({
            '$': {
                'android:name': '.widget.PolarWidgetReceiver',
                'android:exported': 'false',
                'android:label': 'Polar Metrics',
            },
            'intent-filter': [
                {
                    action: [
                        { '$': { 'android:name': 'android.appwidget.action.APPWIDGET_UPDATE' } },
                    ],
                },
            ],
            'meta-data': [
                {
                    '$': {
                        'android:name': 'android.appwidget.provider',
                        'android:resource': '@xml/polar_widget_info',
                    },
                },
            ],
        });

        return config;
    });

    config = withDangerousMod(config, [
        'android',
        async (config) => {
            const projectRoot = config.modRequest.projectRoot;
            const widgetSourceDir = path.join(projectRoot, 'targets/android-widget');
            const androidMainDir = path.join(projectRoot, 'android/app/src/main');
            const kotlinOutputDir = path.join(androidMainDir, 'java/com/polarsource/Polar/widget');
            const resXmlDir = path.join(androidMainDir, 'res/xml');

            fs.mkdirSync(kotlinOutputDir, { recursive: true });
            fs.mkdirSync(resXmlDir, { recursive: true });

            const kotlinFiles = [
                'PolarWidget.kt',
                'PolarWidgetReceiver.kt',
                'MetricsFetcher.kt',
                'MetricsModels.kt',
                'ChartRenderer.kt',
                'WidgetStorageModule.kt'
            ];
            kotlinFiles.forEach(file => {
                const src = path.join(widgetSourceDir, file);
                if (fs.existsSync(src)) {
                    fs.copyFileSync(src, path.join(kotlinOutputDir, file));
                }
            });

            const resDrawableDir = path.join(androidMainDir, 'res/drawable');
            fs.mkdirSync(resDrawableDir, { recursive: true });

            const logoSrc = path.join(projectRoot, 'targets/widget/Assets.xcassets/PolarLogoWhite.imageset/PolarLogoWhite.png');
            if (fs.existsSync(logoSrc)) {
                fs.copyFileSync(logoSrc, path.join(resDrawableDir, 'polar_logo.png'));
            }

            const providerXml = `<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:initialLayout="@layout/glance_default_loading_layout"
    android:minWidth="110dp"
    android:minHeight="110dp"
    android:minResizeWidth="110dp"
    android:minResizeHeight="110dp"
    android:updatePeriodMillis="3600000"
    android:widgetCategory="home_screen">
</appwidget-provider>`;
            fs.writeFileSync(path.join(resXmlDir, 'polar_widget_info.xml'), providerXml);

            return config;
        },
    ]);

    return config;
};

function addDependencies(contents) {
    const dependencies = `
    implementation "androidx.glance:glance-appwidget:1.1.0"
    implementation "androidx.glance:glance-material3:1.1.0"
    implementation "androidx.datastore:datastore-preferences:1.1.1"
    implementation "org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3"
`;
    if (!contents.includes('androidx.glance:glance-appwidget')) {
        return contents.replace(/dependencies\s?{/, `dependencies {${dependencies}`);
    }
    return contents;
}

function addProjectPlugins(contents) {
    if (!contents.includes('kotlinx-serialization')) {
        return contents.replace(/dependencies\s?{/, `dependencies {
        classpath "org.jetbrains.kotlin:kotlin-serialization:1.9.22"`);
    }
    return contents;
}
