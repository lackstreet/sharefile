import { TemplateProps } from "keycloakify/login/TemplateProps";
import { KcContext } from "./KcContext.ts";
import { I18n } from "keycloakify/login/i18n";

type CustomTemplateProps =  Omit<TemplateProps<KcContext,I18n>, "doUseDefaultCss" | "headerNode">
type PageProps<T>={
    kcContext: Extract<KcContext, {pageId: T}>,
    i18n: I18n;
    Template: (props: CustomTemplateProps) => JSX.Element;
}
export type { CustomTemplateProps, PageProps };