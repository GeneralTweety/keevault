const Backbone = require('backbone');
const FileInfoModel = require('../models/file-info-model');
const SettingsStore = require('../comp/settings-store');

const FileInfoCollection = Backbone.Collection.extend({
    model: FileInfoModel,

    initialize: function () {
    },

    load: function (userId) {
        return SettingsStore.load('file-info' + userId).then(data => {
            if (data) {
                this.reset(data, {silent: true});
            }
        });
    },

    save: function (userId) {
        SettingsStore.save('file-info' + userId, this.toJSON());
    },

    getLast: function () {
        return this.first();
    },

    getMatch: function (storage, name, path) {
        return this.find(fi => {
            return (fi.get('storage') || '') === (storage || '') &&
                (fi.get('name') || '') === (name || '') &&
                (fi.get('path') || '') === (path || '');
        });
    },

    getByName: function(name) {
        return this.find(file => file.get('name').toLowerCase() === name.toLowerCase());
    }
});

FileInfoCollection.instance = new FileInfoCollection();

module.exports = FileInfoCollection;
