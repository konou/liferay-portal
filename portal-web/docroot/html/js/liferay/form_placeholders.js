AUI.add(
	'liferay-form-placeholders',
	function(A) {
		var PLACEHOLDER_TEXT_CLASS = 'text-placeholder';

		var SELECTOR_PLACEHOLDER_INPUTS = 'input[placeholder], textarea[placeholder]';

		var STR_BLANK = '';

		var STR_PLACEHOLDER = 'placeholder';

		var Placeholders = A.Component.create(
			{
				addPlaceholder: function(inputTarget) {
					if (!inputTarget.val()) {
						inputTarget.addClass(PLACEHOLDER_TEXT_CLASS);

						inputTarget.val(inputTarget.attr(STR_PLACEHOLDER));
					}
				},
				EXTENDS: A.Plugin.Base,
				NAME: 'placeholders',
				NS: STR_PLACEHOLDER,
				prototype: {
					initializer: function(config) {
						var instance = this;

						var host = instance.get('host');

						var formNode = host.formNode;

						if (formNode) {
							var placeholderInputs = formNode.all(SELECTOR_PLACEHOLDER_INPUTS);

							placeholderInputs.each(A.bind(Placeholders.addPlaceholder, instance));

							instance.host = host;

							instance.beforeHostMethod('_onValidatorSubmit', instance._removePlaceholders, instance);
							instance.beforeHostMethod('_onFieldFocusChange', instance._togglePlaceholders, instance);
						}
					},

					_removePlaceholders: function() {
						var instance = this;

						var formNode = instance.host.formNode;

						var placeholderInputs = formNode.all(SELECTOR_PLACEHOLDER_INPUTS);

						placeholderInputs.each(
							function(item, index, collection) {
								if (item.val() == item.attr(STR_PLACEHOLDER)) {
									item.val(STR_BLANK);
								}
							}
						);
					},

					_togglePlaceholders: function(event) {
						var instance = this;

						var currentTarget = event.currentTarget;

						var placeholder = currentTarget.attr(STR_PLACEHOLDER);

						if (placeholder) {
							var value = currentTarget.val();

							if (event.type == 'focus') {
								if (value == placeholder) {
									currentTarget.val(STR_BLANK);

									currentTarget.removeClass(PLACEHOLDER_TEXT_CLASS);
								}
							}
							else if (!value) {
								Placeholders.addPlaceholder.call(instance, currentTarget);
							}
						}
					}
				}
			}
		);

		Liferay.Form.Placeholders = Placeholders;

		A.Base.plug(Liferay.Form, Placeholders);
	},
	'',
	{
		requires: ['liferay-form', 'plugin']
	}
);