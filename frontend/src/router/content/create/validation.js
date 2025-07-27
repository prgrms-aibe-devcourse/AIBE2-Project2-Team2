export const validateStep = (step, form, categoryProps) => {
  const { selectedCategory1, selectedCategory2, selectedCategory3, category3Options } = categoryProps || {};

  switch (step) {
    case 0:
      return form.title.trim().length > 0 && selectedCategory1 && selectedCategory2 && (category3Options?.length === 0 || selectedCategory3);
    case 1:
      return form.budget && Number(form.budget) > 0;
    case 2:
      return form.description.trim().length > 0;
    case 3:
      return form.thumbnail && form.images && form.images.length > 0;
    default:
      return false;
  }
};
