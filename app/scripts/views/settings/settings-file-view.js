const Backbone = require('backbone');
const OpenConfigView = require('../open-config-view');
const FeatureDetector = require('../../util/feature-detector');
const PasswordGenerator = require('../../util/password-generator');
const Alerts = require('../../comp/alerts');
const Storage = require('../../storage');
const Format = require('../../util/format');
const Locale = require('../../util/locale');
const UrlUtil = require('../../util/url-util');
const FileSaver = require('../../util/file-saver');
const kdbxweb = require('kdbxweb');
const GridView = require('../grid-view');
const Backgrid = require('backgrid');
const PSL = require('../../util/psl');

const DefaultBackupPath = 'Backups/{name}.{date}.bak';
const DefaultBackupSchedule = '1w';

const SettingsFileView = Backbone.View.extend({
    template: require('templates/settings/settings-file.hbs'),

    events: {
        'click .settings__file-button-save-default': 'saveDefault',
        'click .settings__file-button-save-choose': 'toggleChooser',
        'click .settings__file-button-close': 'closeFile',
        'click .settings__file-save-to-file': 'saveToFile',
        'click .settings__file-save-to-xml': 'saveToXml',
        'click .settings__file-save-to-storage': 'saveToStorage',
        'click .settings__file-button-import': 'showImport',
        'change #settings__file-key-file': 'keyFileChange',
        'click #settings__file-file-select-link': 'triggerSelectFile',
        'change #settings__file-file-select': 'fileSelected',
        'focus #settings__file-master-pass': 'focusMasterPass',
        'input #settings__file-master-pass': 'changeMasterPass',
        'blur #settings__file-master-pass': 'blurMasterPass',
        'focus #settings__file-confirm-master-pass': 'focusConfirmMasterPass',
        'blur #settings__file-confirm-master-pass': 'blurConfirmMasterPass',
        'input #settings__file-name': 'changeName',
        'input #settings__file-def-user': 'changeDefUser',
        'change #settings__file-backup-enabled': 'changeBackupEnabled',
        'input #settings__file-backup-path': 'changeBackupPath',
        'change #settings__file-backup-storage': 'changeBackupStorage',
        'change #settings__file-backup-schedule': 'changeBackupSchedule',
        'click .settings__file-button-backup': 'backupFile',
        'change #settings__file-trash': 'changeTrash',
        'input #settings__file-hist-len': 'changeHistoryLength',
        'input #settings__file-hist-size': 'changeHistorySize',
        'input .settings__input-kdf': 'changeKdfParameter',
        'change #settings__file-minURLMatchAccuracyDefault': 'minURLMatchAccuracyDefaultChanged',
        'change #settings__file-placeholders-enabled': 'placeholderEnabledDefaultChanged',
        'change #settings__file-argon2ConfigurationMode': 'changeArgon2Params'
    },

    appModel: null,

    initialize: function() {
        this.listenTo(this.model, 'change:syncing change:syncError change:syncDate', this.deferRender);
    },

    render: function() {
        const storageProviders = [];
        const activeStorage = this.model.get('storage');
        let canBackup = false;
        Object.keys(Storage).forEach(name => {
            const prv = Storage[name];
            if (!canBackup && prv.backup && prv.enabled) {
                canBackup = true;
            }
            if (!prv.system && prv.enabled) {
                storageProviders.push({
                    name: prv.name, icon: prv.icon, iconSvg: prv.iconSvg, own: name === activeStorage, backup: prv.backup
                });
            }
        });
        storageProviders.sort((x, y) => (x.uipos || Infinity) - (y.uipos || Infinity));
        const backup = this.model.get('backup');
        const browserName = FeatureDetector.getBrowserExtensionType();
        this.renderTemplate({
            cmd: FeatureDetector.actionShortcutSymbol(true),
            supportFiles: false,
            name: this.model.get('name'),
            path: this.model.get('path'),
            storage: this.model.get('storage'),
            readonly: this.appModel.get('readOnly'),
            syncing: this.model.get('syncing'),
            syncError: this.model.get('syncError'),
            syncDate: Format.dtStr(this.model.get('syncDate')),
            password: PasswordGenerator.present(this.model.get('passwordLength')),
            defaultUser: this.model.get('defaultUser'),
            recycleBinEnabled: this.model.get('recycleBinEnabled'),
            backupEnabled: backup && backup.enabled,
            backupStorage: backup && backup.storage,
            backupPath: backup && backup.path || DefaultBackupPath.replace('{name}', this.model.get('name')),
            backupSchedule: backup ? backup.schedule : DefaultBackupSchedule,
            historyMaxItems: this.model.get('historyMaxItems'),
            historyMaxSize: Math.round(this.model.get('historyMaxSize') / 1024 / 1024),
            keyEncryptionRounds: this.model.get('keyEncryptionRounds'),
            kdfParameters: this.kdfParametersToUi(this.model.get('kdfParameters')),
            storageProviders: storageProviders,
            canBackup: canBackup,
            displayGlobalPlaceholderOption: this.model.get('browserExtensionSettings').get('displayGlobalPlaceholderOption'),
            placeholdersEnabled: this.model.get('browserExtensionSettings').get('defaultPlaceholderHandling') === 'Enabled',
            minURLMatchAccuracyDefault: this.model.get('browserExtensionSettings').get('defaultMatchAccuracy'),
            argon2Configuration: this.model.getArgon2StrengthMode(),
            extensionLink: FeatureDetector.getExtensionInstallURL(),
            extensionAvailable: !!browserName,
            extensionEnabled: window.keeAddonEnabled,
            browserName: browserName,
            modified: this.model.get('modified')
        });
        if (!this.model.get('created')) {
            this.$el.find('.settings__file-master-pass-warning').toggle(this.model.get('passwordChanged'));
            this.$el.find('#settings__file-master-pass-warning-text').text(Locale.setFilePassChanged);
        }
        this.renderKeyFileSelect();
        this.renderAddonSettings();
        return this;
    },

    kdfParametersToUi: function(kdfParameters) {
        return kdfParameters ? _.extend({}, kdfParameters, { memory: Math.round(kdfParameters.memory / 1024) }) : null;
    },

    renderAddonSettings: function () {
        const columns = [{
            name: 'domain',
            label: Locale.domain,
            cell: 'string',
            sortable: false
        }, {
            name: 'method',
            label: Locale.method,
            // TODO: Browsers don't support auto-opening of a select control so the user has to
            // click twice which is unintuitive and time consuming. Only way to avoid this would
            // be to develop a custom select box control alternative and hook that into the
            // BackGrid thing instead. Also will want to ensure focus is set so that the user
            // can at least use a keyboard arrow to save a bit of time.
            cell: Backgrid.SelectCell.extend({optionValues: [
                [Locale.domain, 'Domain'],
                [Locale.hostname, 'Hostname'],
                [Locale.exact, 'Exact']]}),
            sortable: false
        }];

        const collection = this.model.get('browserExtensionSettings').get('matchedURLAccuracyOverrides');

        // Initialize a new Grid instance
        const grid = new GridView({
            columns: columns,
            collection: collection,
            newRowConfig: {
                validate: (text) => {
                    if (PSL.getDomain(text)) return true;
                    return false;
                },
                addItem: (text) => {
                    collection.add({
                        domain: text,
                        method: 'Domain'
                    });
                },
                placeholder: 'Domain (e.g. google.com)'
            }
        });

        $('#settings__file-minURLMatchAccuracyOverrides').after(grid.render().el);
    },

    renderKeyFileSelect: function() {
        const keyFileName = this.model.get('keyFileName');
        const oldKeyFileName = this.model.get('oldKeyFileName');
        const keyFileChanged = this.model.get('keyFileChanged');
        const sel = this.$el.find('#settings__file-key-file');
        sel.html('');
        if (keyFileName && keyFileChanged) {
            const text = keyFileName !== 'Generated' ? Locale.setFileUseKeyFile + ' ' + keyFileName : Locale.setFileUseGenKeyFile;
            $('<option/>').val('ex').text(text).appendTo(sel);
        }
        if (oldKeyFileName) {
            const useText = keyFileChanged ? Locale.setFileUseOldKeyFile : Locale.setFileUseKeyFile + ' ' + oldKeyFileName;
            $('<option/>').val('old').text(useText).appendTo(sel);
        }
        $('<option/>').val('gen').text(Locale.setFileGenKeyFile).appendTo(sel);
        $('<option/>').val('none').text(Locale.setFileDontUseKeyFile).appendTo(sel);
        if (keyFileName && keyFileChanged) {
            sel.val('ex');
        } else if (!keyFileName) {
            sel.val('none');
        } else if (oldKeyFileName && keyFileName === oldKeyFileName && !keyFileChanged) {
            sel.val('old');
        }
    },

    validatePassword: function(continueCallback) {
        if (!this.model.get('passwordLength')) {
            Alerts.yesno({
                header: Locale.setFileEmptyPass,
                body: Locale.setFileEmptyPassBody,
                success: () => {
                    continueCallback();
                },
                cancel: () => {
                    this.$el.find('#settings__file-master-pass').focus();
                }
            });
            return false;
        }
        return true;
    },

    save: function(arg) {
        if (!arg) {
            arg = {};
        }
        arg.startedByUser = true;
        if (!arg.skipValidation) {
            const isValid = this.validatePassword(() => {
                arg.skipValidation = true;
                this.save(arg);
            });
            if (!isValid) {
                return;
            }
        }
        this.appModel.syncFile(this.model, arg);
    },

    saveDefault: function() {
        this.save();
    },

    toggleChooser: function() {
        this.$el.find('.settings__file-save-choose').toggleClass('hide');
    },

    showImport: function () {
        if (!this.model.get('demo')) {
            Backbone.trigger('show-import', { firstRun: false });
        } else {
            Alerts.info({
                header: Locale.registerNow,
                body: Locale.importNotSupportedDemo,
                buttons: [
                    { result: 'cancel', title: Locale.alertCancel, error: true },
                    { result: 'register', title: Locale.registerNow }
                ],
                success: (result) => {
                    if (result === 'register') {
                        Backbone.trigger('show-registration');
                    }
                }
            });
        }
    },

    saveToFile: function(skipValidation) {
        if (skipValidation !== true && !this.validatePassword(this.saveToFile.bind(this, true))) {
            return;
        }
        const fileName = this.model.get('name') + '.kdbx';
        this.model.getData(data => {
            if (!data) {
                return;
            }
            const blob = new Blob([data], {type: 'application/octet-stream'});
            FileSaver.saveAs(blob, fileName);
        });
    },

    saveToXml: function() {
        this.model.getXml(xml => {
            const blob = new Blob([xml], {type: 'text/xml'});
            FileSaver.saveAs(blob, this.model.get('name') + '.xml');
        });
    },

    saveToStorage: function(e) {
        if (this.model.get('syncing') || this.model.get('demo')) {
            return;
        }
        const storageName = $(e.target).closest('.settings__file-save-to-storage').data('storage');
        const storage = Storage[storageName];
        if (!storage) {
            return;
        }
        if (this.model.get('storage') === storageName) {
            this.save();
        } else {
            if (!storage.list) {
                if (storage.getOpenConfig) {
                    const config = _.extend({
                        id: storage.name,
                        name: Locale[storage.name] || storage.name,
                        icon: storage.icon,
                        buttons: false
                    }, storage.getOpenConfig());
                    const openConfigView = new OpenConfigView({ model: config });
                    Alerts.alert({
                        header: '',
                        body: '',
                        icon: storage.icon || 'files-o',
                        buttons: [Alerts.buttons.ok, Alerts.buttons.cancel],
                        esc: '',
                        opaque: true,
                        view: openConfigView,
                        success: () => {
                            const storageConfig = openConfigView.getData();
                            if (!storageConfig) {
                                return;
                            }
                            const opts = _.omit(storageConfig, ['path', 'storage']);
                            if (opts && Object.keys(opts).length) {
                                this.model.set('opts', opts);
                            }
                            this.save({ storage: storageName, path: storageConfig.path, opts });
                        }
                    });
                } else {
                    Alerts.notImplemented();
                }
                return;
            }
            this.model.set('syncing', true);
            storage.list('', (err, files) => {
                this.model.set('syncing', false);
                if (err) {
                    return;
                }
                const expName = this.model.get('name').toLowerCase();
                const existingFile = _.find(files, file => {
                    return !file.dir && UrlUtil.getDataFileName(file.name).toLowerCase() === expName;
                });
                if (existingFile) {
                    Alerts.yesno({
                        header: Locale.setFileAlreadyExists,
                        body: Locale.setFileAlreadyExistsBody.replace('{}', this.model.name),
                        success: () => {
                            this.model.set('syncing', true);
                            storage.remove(existingFile.path, err => {
                                this.model.set('syncing', false);
                                if (!err) {
                                    this.save({storage: storageName});
                                }
                            });
                        }
                    });
                } else {
                    this.save({storage: storageName});
                }
            });
        }
    },

    closeFile: function() {
        if (this.model.get('modified')) {
            Alerts.yesno({
                header: Locale.setFileUnsaved,
                body: Locale.setFileUnsavedBody,
                buttons: [
                    {result: 'close', title: Locale.setFileCloseNoSave, error: true},
                    {result: '', title: Locale.setFileDontClose}
                ],
                success: result => {
                    if (result === 'close') {
                        this.closeFileNoCheck();
                    }
                }
            });
        } else {
            if (!this.model.get('demo')) {
                this.closeFileNoCheck();
            } else {
                Alerts.info({
                    header: Locale.registerNow,
                    body: Locale.willCloseDemo,
                    buttons: [
                        { result: 'cancel', title: Locale.alertCancel, error: true },
                        { result: 'register', title: Locale.registerNow }
                    ],
                    success: (result) => {
                        if (result === 'register') {
                            Backbone.trigger('show-registration');
                        }
                    }
                });
            }
        }
    },

    closeFileNoCheck: function() {
        this.appModel.closeFile(this.model);
    },

    minURLMatchAccuracyDefaultChanged: function(e) {
        this.model.get('browserExtensionSettings').set('defaultMatchAccuracy', e.target.value);
    },

    placeholderEnabledDefaultChanged: function(e) {
        this.model.get('browserExtensionSettings').set('defaultPlaceholderHandling', e.target.checked ? 'Enabled' : 'Disabled');
        if (e.target.checked) {
            $('#settings__file-placeholders-enabled-warning').removeClass('hide');
        } else {
            $('#settings__file-placeholders-enabled-warning').addClass('hide');
        }
    },

    keyFileChange: function(e) {
        switch (e.target.value) {
            case 'old':
                this.selectOldKeyFile();
                break;
            case 'gen':
                this.generateKeyFile();
                break;
            case 'none':
                this.clearKeyFile();
                break;
        }
    },

    selectOldKeyFile: function() {
        this.model.resetKeyFile();
        this.renderKeyFileSelect();
    },

    generateKeyFile: function() {
        const keyFile = this.model.generateAndSetKeyFile();
        const blob = new Blob([keyFile], {type: 'application/octet-stream'});
        FileSaver.saveAs(blob, this.model.get('name') + '.key');
        this.renderKeyFileSelect();
    },

    clearKeyFile: function() {
        this.model.removeKeyFile();
        this.renderKeyFileSelect();
    },

    triggerSelectFile: function() {
        this.$el.find('#settings__file-file-select').click();
    },

    fileSelected: function(e) {
        const file = e.target.files[0];
        const reader = new FileReader();
        reader.onload = e => {
            const res = e.target.result;
            this.model.setKeyFile(res, file.name);
            this.renderKeyFileSelect();
        };
        reader.readAsArrayBuffer(file);
    },

    focusMasterPass: function(e) {
        e.target.value = '';
        e.target.setAttribute('type', 'text');
        this.model.set('passwordChanged', false);
    },

    changeMasterPass: function(e) {
        if (!e.target.value) {
            this.model.resetPassword();
            this.$el.find('.settings__file-master-pass-warning').hide();
        } else {
            this.$el.find('#settings__file-confirm-master-pass-group').show();
            this.$el.find('#settings__file-master-pass-warning-text').text(Locale.setFilePassChange);
            if (!this.model.get('created')) {
                this.$el.find('.settings__file-master-pass-warning').show();
            }
        }
    },

    blurMasterPass: function(e) {
        if (!e.target.value) {
            this.model.resetPassword();
            this.resetConfirmMasterPass();
            e.target.value = PasswordGenerator.present(this.model.get('passwordLength'));
            this.$el.find('.settings__file-master-pass-warning').hide();
        }
        e.target.setAttribute('type', 'password');
    },

    resetConfirmMasterPass: function() {
        this.$el.find('#settings__file-confirm-master-pass').val('');
        this.$el.find('#settings__file-confirm-master-pass-group').hide();
        this.$el.find('#settings__file-master-pass-warning-text').text(Locale.setFilePassChange);
    },

    focusConfirmMasterPass: function(e) {
        e.target.value = '';
        e.target.setAttribute('type', 'text');
    },

    blurConfirmMasterPass: function(e) {
        e.target.setAttribute('type', 'password');
        const masterPassword = this.$el.find('#settings__file-master-pass').val();
        const confirmPassword = e.target.value;
        if (masterPassword === confirmPassword) {
            this.$el.find('#settings__file-master-pass-warning-text').text(Locale.setFilePassChanged);
            this.$el.find('.settings__file-confirm-master-pass-warning').hide();
            this.model.setPassword(kdbxweb.ProtectedValue.fromString(confirmPassword, []));
        } else {
            this.$el.find('#settings__file-master-pass-warning-text').text(Locale.setFilePassChange);
            this.$el.find('.settings__file-confirm-master-pass-warning').show();
            this.model.resetPassword();
        }
    },

    changeName: function(e) {
        const value = $.trim(e.target.value);
        if (!value) {
            return;
        }
        this.model.setName(value);
    },

    changeDefUser: function(e) {
        const value = $.trim(e.target.value);
        this.model.setDefaultUser(value);
    },

    changeBackupEnabled: function(e) {
        const enabled = e.target.checked;
        let backup = this.model.get('backup');
        if (!backup) {
            backup = { enabled: enabled, schedule: DefaultBackupSchedule };
            const defaultPath = DefaultBackupPath.replace('{name}', this.model.get('name'));
            backup.storage = 'dropbox';
            backup.path = defaultPath;
            // } else if (this.model.get('storage') === 'webdav') {
            //     backup.storage = 'webdav';
            //     backup.path = this.model.get('path') + '.{date}.bak';
            // } else if (this.model.get('storage')) {
            //     backup.storage = this.model.get('storage');
            //     backup.path = DefaultBackupPath.replace('{name}', this.model.get('name'));
            // } else {
            //     Object.keys(Storage).forEach(name => {
            //         var prv = Storage[name];
            //         if (!backup.storage && !prv.system && prv.enabled) {
            //             backup.storage = name;
            //         }
            //     });
            //     if (!backup.storage) {
            //         e.target.checked = false;
            //         return;
            //     }
            //     backup.path = DefaultBackupPath.replace('{name}', this.model.get('name'));
            // }
            this.$el.find('#settings__file-backup-storage').val(backup.storage);
            this.$el.find('#settings__file-backup-path').val(backup.path);
        }
        this.$el.find('.settings__file-backups').toggleClass('hide', !enabled);
        backup.enabled = enabled;
        this.setBackup(backup);
    },

    changeBackupPath: function(e) {
        const backup = this.model.get('backup');
        backup.path = e.target.value.trim();
        this.setBackup(backup);
    },

    changeBackupStorage: function(e) {
        const backup = this.model.get('backup');
        backup.storage = e.target.value;
        this.setBackup(backup);
    },

    changeBackupSchedule: function(e) {
        const backup = this.model.get('backup');
        backup.schedule = e.target.value;
        this.setBackup(backup);
    },

    setBackup: function(backup) {
        this.model.set('backup', backup);
        this.appModel.setFileBackup(this.model.id, backup);
    },

    backupFile: function() {
        if (this.backupInProgress) {
            return;
        }
        const backupButton = this.$el.find('.settings__file-button-backup');
        backupButton.text(Locale.setFileBackupNowWorking);
        this.model.getData(data => {
            if (!data) {
                this.backupInProgress = false;
                backupButton.text(Locale.setFileBackupNow);
                return;
            }
            this.appModel.backupFile(this.model, data, (err) => {
                this.backupInProgress = false;
                backupButton.text(Locale.setFileBackupNow);
                if (err) {
                    Alerts.error({
                        title: Locale.setFileBackupError,
                        body: Locale.setFileBackupErrorDescription,
                        pre: err.toString()
                    });
                }
            });
        });
    },

    changeTrash: function(e) {
        this.model.setRecycleBinEnabled(e.target.checked);
    },

    changeHistoryLength: function(e) {
        if (!e.target.validity.valid) {
            return;
        }
        const value = +e.target.value;
        if (isNaN(value)) {
            e.target.value = this.model.get('historyMaxItems');
            return;
        }
        this.model.setHistoryMaxItems(value);
    },

    changeHistorySize: function(e) {
        if (!e.target.validity.valid) {
            return;
        }
        const value = +e.target.value;
        if (isNaN(value)) {
            e.target.value = this.model.get('historyMaxSize') / 1024 / 1024;
            return;
        }
        this.model.setHistoryMaxSize(value * 1024 * 1024);
    },

    changeKdfParameter: function(e) {
        if (!e.target.validity.valid) {
            return;
        }
        const field = $(e.target).data('field');
        const mul = $(e.target).data('mul') || 1;
        const value = e.target.value * mul;
        if (isNaN(value)) {
            e.target.value = Math.round(this.model.get('kdfParameters')[field] / mul);
            return;
        }
        if (value > 0) {
            this.model.setKdfParameter(field, value);
        }
    },

    changeArgon2Params: function(e) {
        const val = e.target.value;
        if (val === '0') {
            $('#settings__file-argon2CustomSettings').show();
        } else {
            $('#settings__file-argon2CustomSettings').hide();

            // Spidermoney Firefox v63 passes a string into
            // configureArgon2ParamsManual unless we assign it to a local var first!
            const valNum = parseInt(val);
            this.model.configureArgon2ParamsManual(valNum);

            // Update the individual parameters so that the user can use them as starting point for customisation
            const params = this.kdfParametersToUi(this.model.get('kdfParameters'));
            $('#settings__file-kdf-iter').val(params.iterations);
            $('#settings__file-kdf-mem').val(params.memory);
            $('#settings__file-kdf-par').val(params.parallelism);
        }
    }
});

module.exports = SettingsFileView;
