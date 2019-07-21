
const Capacitor = require('@capacitor/core');
const Logger = require('../util/logger');
const KPRPCHandler = require('../comp/keepassrpc');
const logger = new Logger('nativeCache');

const mapModel = function(model) {
    const state = {
        id: model.account.get('user').emailHashed,
        config: {
            expiry: 12,
            requireFullKey: false
            // actual hashes of mini-keys "last 5 chars, etc." can be sent separately I think?
        },
        vault: KPRPCHandler.invokeLocal.getAllDatabases(true)
    };
    logger.debug('All private data: ' + JSON.stringify(state));
    return state;
};

const NativeCache = {

    update: function (model) {
        // if (model.deviceInfo.platform === 'web') return;
        try {
            Capacitor.Plugins.NativeCache.update(mapModel(model));
        } catch (e) {
            logger.error('Failed to send data to native cache: ' + e);
        }
    }
};

module.exports = NativeCache;
