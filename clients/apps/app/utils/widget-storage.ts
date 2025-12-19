import { ExtensionStorage } from '@bacons/apple-targets';
import { NativeModules, Platform } from 'react-native';

const IOS_GROUP_ID = 'group.com.polarsource.Polar';
const appleStorage = new ExtensionStorage(IOS_GROUP_ID);

const WidgetStorageModule = NativeModules.WidgetStorage;

export const setWidgetValue = (key: string, value: string) => {
    if (Platform.OS === 'ios') {
        appleStorage.set(key, value);
    } else if (Platform.OS === 'android') {
        if (WidgetStorageModule) {
            WidgetStorageModule.setItem(key, value);
        }
    }
};

export const clearWidgetValues = () => {
    const keys = ['widget_api_token', 'widget_organization_id', 'widget_organization_name'];
    keys.forEach(key => setWidgetValue(key, ''));
};
